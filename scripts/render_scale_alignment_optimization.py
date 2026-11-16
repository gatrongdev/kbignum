#!/usr/bin/env python3
"""Create a focused KBigDecimal scale-alignment A/B CSV and speedup chart."""

from __future__ import annotations

import argparse
import csv
import html
import json
import platform
import re
import xml.etree.ElementTree as ET
from datetime import datetime, timezone
from pathlib import Path


NODE_RESULT = re.compile(
    r"DECIMAL_SCALE_ALIGNMENT_BENCHMARK digits=(\d+) scaleGap=(\d+) "
    r"operation=(\w+).*?samples=(\d+).*?runtime=([^ ]+) platform=([^ ]+) medianNs=([0-9.]+)"
)
NODE_OPERATIONS = {
    "addSameScale": ("AddSameScale", 0),
    "addMismatchedScale": ("Add", None),
    "subtractMismatchedScale": ("Subtract", None),
    "compareMismatchedScale": ("CompareTo", None),
    "addZeroMismatchedScale": ("AddZero", None),
    "subtractZeroMismatchedScale": ("SubtractZero", None),
    "compareZeroMismatchedScale": ("CompareZero", None),
}
JMH_ROWS = {
    "scaleAlignmentAddKBigNum": ("Add", False),
    "scaleAlignmentSubtractKBigNum": ("Subtract", False),
    "scaleAlignmentCompareKBigNum": ("CompareTo", False),
    "scaleAlignmentAddZeroKBigNum": ("AddZero", False),
    "scaleAlignmentSubtractZeroKBigNum": ("SubtractZero", False),
    "scaleAlignmentCompareZeroKBigNum": ("CompareZero", False),
    "decimalAddKBigNum": ("AddSameScale", True),
    "decimalSubtractKBigNum": ("SubtractSameScale", True),
}
CSV_FIELDS = [
    "runtime",
    "operation",
    "digits",
    "scale_gap",
    "baseline_ns_op",
    "baseline_ci_lower_ns_op",
    "baseline_ci_upper_ns_op",
    "candidate_ns_op",
    "candidate_ci_lower_ns_op",
    "candidate_ci_upper_ns_op",
    "speedup_baseline_over_candidate",
    "baseline_bytes_op",
    "candidate_bytes_op",
    "evidence",
    "runtime_version",
    "platform",
    "generated_utc",
]


def jmh_rows(paths: list[Path]) -> dict[tuple[str, int, int], dict[str, float]]:
    rows: dict[tuple[str, int, int], dict[str, float]] = {}
    for path in paths:
        for entry in json.loads(path.read_text(encoding="utf-8")):
            method = str(entry.get("benchmark", "")).rsplit(".", 1)[-1]
            if method not in JMH_ROWS or not method.endswith("KBigNum"):
                continue
            operation, same_scale = JMH_ROWS[method]
            params = entry.get("params", {})
            digits = int(params["digits"])
            gap = 0 if same_scale else int(params["scaleGap"])
            metric = entry["primaryMetric"]
            score = float(metric["score"])
            confidence = metric.get("scoreConfidence", [score, score])
            rows[(operation, digits, gap)] = {
                "score": score,
                "lower": float(confidence[0]),
                "upper": float(confidence[1]),
            }
    return rows


def gc_rows(path: Path) -> dict[tuple[str, int, int], float]:
    rows: dict[tuple[str, int, int], float] = {}
    for entry in json.loads(path.read_text(encoding="utf-8")):
        method = str(entry.get("benchmark", "")).rsplit(".", 1)[-1]
        if method not in JMH_ROWS or not method.startswith("scaleAlignment") or not method.endswith("KBigNum"):
            continue
        operation, _ = JMH_ROWS[method]
        params = entry["params"]
        metric = entry.get("secondaryMetrics", {}).get("·gc.alloc.rate.norm")
        if metric is not None:
            rows[(operation, int(params["digits"]), int(params["scaleGap"]))] = float(metric["score"])
    return rows


def node_rows(path: Path) -> tuple[dict[tuple[str, int, int], dict[str, float | str]], str, str]:
    root = ET.parse(path).getroot()
    output: dict[tuple[str, int, int], dict[str, float | str]] = {}
    runtime = "unknown"
    node_platform = "unknown"
    for match in NODE_RESULT.finditer(root.findtext("system-out", "")):
        digits, gap, raw_operation, samples, found_runtime, found_platform, score = match.groups()
        if raw_operation not in NODE_OPERATIONS:
            continue
        operation, fixed_gap = NODE_OPERATIONS[raw_operation]
        result_gap = int(gap) if fixed_gap is None else fixed_gap
        key = (operation, int(digits), result_gap)
        # Same-scale samples are emitted inside both gap sweeps; retain the first pair once.
        if key in output:
            continue
        output[key] = {"score": float(score), "samples": int(samples)}
        runtime = found_runtime
        node_platform = found_platform
    return output, runtime, node_platform


def paired_row(
    runtime: str,
    operation: str,
    digits: int,
    gap: int,
    baseline: dict[str, float],
    candidate: dict[str, float],
    *,
    baseline_bytes: float | None = None,
    candidate_bytes: float | None = None,
    evidence: str,
    runtime_version: str,
    system: str,
    generated: str,
) -> dict[str, object]:
    speedup = baseline["score"] / candidate["score"]
    if evidence == "JMH 99.9% CI":
        separated = baseline["upper"] < candidate["lower"] or candidate["upper"] < baseline["lower"]
        evidence_label = "JMH 99.9% intervals separated" if separated else "JMH 99.9% intervals overlap"
    else:
        evidence_label = evidence
    return {
        "runtime": runtime,
        "operation": operation,
        "digits": digits,
        "scale_gap": gap,
        "baseline_ns_op": baseline["score"],
        "baseline_ci_lower_ns_op": baseline.get("lower", ""),
        "baseline_ci_upper_ns_op": baseline.get("upper", ""),
        "candidate_ns_op": candidate["score"],
        "candidate_ci_lower_ns_op": candidate.get("lower", ""),
        "candidate_ci_upper_ns_op": candidate.get("upper", ""),
        "speedup_baseline_over_candidate": speedup,
        "baseline_bytes_op": baseline_bytes if baseline_bytes is not None else "",
        "candidate_bytes_op": candidate_bytes if candidate_bytes is not None else "",
        "evidence": evidence_label,
        "runtime_version": runtime_version,
        "platform": system,
        "generated_utc": generated,
    }


def render_svg(rows: list[dict[str, object]], generated: str, jvm_version: str, node_version: str) -> str:
    selected = [
        row
        for row in rows
        if row["digits"] == 1000
        and row["operation"] in ("AddZero", "CompareTo", "CompareZero")
        and row["scale_gap"] in (8, 128)
    ]
    width, panel_height = 1200, 330
    height = 125 + 2 * panel_height + 50
    plot_left, plot_right = 300, 1125
    plot_width = plot_right - plot_left
    max_speedup = 10.0
    colors = {"AddZero": "#15803d", "CompareTo": "#2563eb", "CompareZero": "#7c3aed"}
    operation_labels = {
        "AddZero": "Add zero + value",
        "CompareTo": "Compare nonzero",
        "CompareZero": "Compare zero + value",
    }
    parts = [
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}">',
        '<rect width="100%" height="100%" fill="#fff"/>',
        (
            '<style>text{font-family:Arial,Helvetica,sans-serif;fill:#172033}'
            '.muted{fill:#64748b}.grid{stroke:#d8dee8;stroke-width:1}'
            '.axis{stroke:#64748b;stroke-width:1}</style>'
        ),
        '<text x="45" y="42" font-size="25" font-weight="700">KBigDecimal scale-alignment optimization</text>',
        (
            f'<text x="45" y="68" font-size="13" class="muted">1000 digits · '
            f'speedup = baseline ÷ candidate · generated {html.escape(generated)}</text>'
        ),
        (
            f'<text x="45" y="88" font-size="12" class="muted">JVM JMH 99.9% intervals shown in CSV · '
            f'Node.js {html.escape(node_version)} sample medians have no confidence interval</text>'
        ),
    ]
    panels = (("JVM", "JVM (JMH)", jvm_version), ("Kotlin/JS Node.js", "Kotlin/JS Node.js", node_version))
    for panel_index, (runtime, title, version) in enumerate(panels):
        y0 = 110 + panel_index * panel_height
        panel_rows = [row for row in selected if row["runtime"] == runtime]
        parts.append(
            f'<rect x="35" y="{y0}" width="1130" height="{panel_height - 12}" '
            'rx="8" fill="#fff" stroke="#d8dee8"/>'
        )
        parts.append(
            f'<text x="52" y="{y0 + 24}" font-size="16" font-weight="700">'
            f'{title} · {html.escape(version)}</text>'
        )
        for tick in range(0, 11, 2):
            x = plot_left + (tick / max_speedup) * plot_width
            parts.append(f'<line x1="{x:.1f}" y1="{y0 + 48}" x2="{x:.1f}" y2="{y0 + panel_height - 42}" class="grid"/>')
            parts.append(
                f'<text x="{x:.1f}" y="{y0 + panel_height - 22}" text-anchor="middle" '
                f'font-size="10" class="muted">{tick}×</text>'
            )
        one_x = plot_left + plot_width / max_speedup
        parts.append(
            f'<line x1="{one_x:.1f}" y1="{y0 + 48}" x2="{one_x:.1f}" '
            f'y2="{y0 + panel_height - 42}" stroke="#64748b" stroke-dasharray="4 3"/>'
        )
        for index, (operation, gap) in enumerate((
            ("AddZero", 8), ("AddZero", 128),
            ("CompareTo", 8), ("CompareTo", 128),
            ("CompareZero", 8), ("CompareZero", 128),
        )):
            row = next(
                (
                    item
                    for item in panel_rows
                    if item["operation"] == operation and item["scale_gap"] == gap
                ),
                None,
            )
            if row is None:
                continue
            y = y0 + 62 + index * 37
            label = f'{operation_labels[operation]} · gap {gap}'
            parts.append(f'<text x="55" y="{y + 13}" font-size="12">{label}</text>')
            speedup = float(row["speedup_baseline_over_candidate"])
            bar_end = plot_left + min(speedup, max_speedup) / max_speedup * plot_width
            marker = (
                "†"
                if runtime == "JVM" and "overlap" in str(row["evidence"])
                else ("*" if runtime != "JVM" else "")
            )
            parts.append(
                f'<rect x="{plot_left}" y="{y}" width="{max(1.0, bar_end - plot_left):.1f}" '
                f'height="18" rx="3" fill="{colors[operation]}"/>'
            )
            display = f'{speedup:.2f}×{marker}'
            label_x = min(bar_end + 8, plot_right - 48)
            parts.append(f'<text x="{label_x:.1f}" y="{y + 13}" font-size="11" font-weight="700">{display}</text>')
    parts.extend([
        (
            '<text x="45" y="800" font-size="11" class="muted">† JMH baseline and candidate intervals overlap; '
            '* Node.js reports sample medians only. Values above 1× favor the candidate.</text>'
        ),
        '</svg>',
    ])
    return "\n".join(parts)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    for name in ("jvm-baseline", "jvm-candidate"):
        parser.add_argument(f"--{name}", type=Path, nargs="+", required=True)
    for name in ("gc-baseline", "gc-candidate", "node-baseline", "node-candidate"):
        parser.add_argument(f"--{name}", type=Path, required=True)
    parser.add_argument("--csv", type=Path, default=Path("benchmarks/results/decimal-scale-alignment-optimization.csv"))
    parser.add_argument(
        "--svg",
        type=Path,
        default=Path("docs/assets/benchmark-decimal-scale-alignment-optimization.svg"),
    )
    args = parser.parse_args()

    jvm_baseline = jmh_rows(args.jvm_baseline)
    jvm_candidate = jmh_rows(args.jvm_candidate)
    gc_baseline = gc_rows(args.gc_baseline)
    gc_candidate = gc_rows(args.gc_candidate)
    node_baseline, node_version, node_platform = node_rows(args.node_baseline)
    node_candidate, candidate_node_version, candidate_node_platform = node_rows(args.node_candidate)
    if node_version != candidate_node_version or node_platform != candidate_node_platform:
        raise ValueError("Baseline and candidate Node.js runtime/platform differ")

    generated = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M UTC")
    rows: list[dict[str, object]] = []
    for key, baseline in jvm_baseline.items():
        if key not in jvm_candidate:
            raise ValueError(f"Missing candidate JMH measurement for {key}")
        rows.append(
            paired_row(
                "JVM",
                key[0],
                key[1],
                key[2],
                baseline,
                jvm_candidate[key],
                baseline_bytes=gc_baseline.get(key),
                candidate_bytes=gc_candidate.get(key),
                evidence="JMH 99.9% CI",
                runtime_version="JDK 21.0.8 / JMH 1.36",
                system=f"{platform.system()} {platform.release()} / {platform.machine()}",
                generated=generated,
            )
        )
    if set(jvm_baseline) != set(jvm_candidate):
        raise ValueError("Baseline and candidate JMH benchmark keys differ")

    if set(node_baseline) != set(node_candidate):
        raise ValueError("Baseline and candidate Node.js benchmark keys differ")
    for key, baseline in node_baseline.items():
        candidate = node_candidate[key]
        rows.append(
            paired_row(
                "Kotlin/JS Node.js",
                key[0],
                key[1],
                key[2],
                {"score": float(baseline["score"])},
                {"score": float(candidate["score"])},
                evidence=f"median of {baseline['samples']} samples/run",
                runtime_version=node_version,
                system=node_platform,
                generated=generated,
            )
        )

    rows.sort(key=lambda row: (str(row["runtime"]), int(row["digits"]), int(row["scale_gap"]), str(row["operation"])))
    args.csv.parent.mkdir(parents=True, exist_ok=True)
    with args.csv.open("w", newline="", encoding="utf-8") as csv_file:
        writer = csv.DictWriter(csv_file, fieldnames=CSV_FIELDS, lineterminator="\n")
        writer.writeheader()
        writer.writerows(rows)
    args.svg.parent.mkdir(parents=True, exist_ok=True)
    args.svg.write_text(render_svg(rows, generated, "21.0.8+9-b1140.54", node_version), encoding="utf-8")
    print(f"Wrote {args.csv} ({len(rows)} paired measurements)")
    print(f"Wrote {args.svg}")


if __name__ == "__main__":
    main()
