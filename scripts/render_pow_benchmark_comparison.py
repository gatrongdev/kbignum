#!/usr/bin/env python3
"""Render a before/after comparison for the KBigInteger and KBigDecimal pow benchmarks."""

from __future__ import annotations

import argparse
import csv
import html
import json
import math
import re
import xml.etree.ElementTree as ET
from datetime import datetime, timezone
from pathlib import Path


BITS = (1024, 2048, 4096, 8192)
OPERATIONS = (
    ("integerPowKBigNum", "KBigInteger.pow(5)", "#dc2626"),
    ("decimalPowKBigNum", "KBigDecimal.pow(5)", "#7c3aed"),
)


def load_results(path: Path) -> tuple[dict[str, dict[int, dict[str, float]]], str, str]:
    entries = json.loads(path.read_text(encoding="utf-8"))
    results: dict[str, dict[int, dict[str, float]]] = {method: {} for method, _, _ in OPERATIONS}
    jmh_version = "unknown"
    jvm_version = "unknown"

    for entry in entries:
        method = str(entry.get("benchmark", "")).rsplit(".", 1)[-1]
        if method not in results:
            continue
        bits = int(entry["params"]["bits"])
        metric = entry["primaryMetric"]
        score = float(metric["score"])
        confidence = metric.get("scoreConfidence", [score, score])
        lower = float(confidence[0]) if len(confidence) > 0 else score
        upper = float(confidence[1]) if len(confidence) > 1 else score
        if bits not in BITS or not all(math.isfinite(value) for value in (score, lower, upper)) or score <= 0:
            raise ValueError(f"Invalid {method} result for {bits} bits")
        if bits in results[method]:
            raise ValueError(f"Duplicate {method} result for {bits} bits")
        results[method][bits] = {"score": score, "lower": lower, "upper": upper}
        jmh_version = str(entry.get("jmhVersion", jmh_version))
        jvm_version = str(entry.get("vmVersion", jvm_version))

    missing = [f"{method}/{bits}" for method in results for bits in BITS if bits not in results[method]]
    if missing:
        raise ValueError("Missing JMH results: " + ", ".join(missing))
    return results, jmh_version, jvm_version


def render_svg(rows: list[dict[str, object]], generated_at: str, jmh_version: str, jvm_version: str) -> str:
    width = 1120
    height = 560
    panel_width = 500
    panel_lefts = (70, 580)
    plot_top = 145
    plot_bottom = 445
    maximum = max(float(row["speedup"]) for row in rows)
    y_max = max(3.0, math.ceil((maximum + 0.25) * 2) / 2)
    parts = [
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}">',
        '<rect width="100%" height="100%" fill="#ffffff"/>',
        '<style>text{font-family:Arial,Helvetica,sans-serif;fill:#172033}.muted{fill:#5b6474}.grid{stroke:#d8dee8;stroke-width:1}.axis{stroke:#64748b;stroke-width:1}</style>',
        '<text x="60" y="42" font-size="25" font-weight="700">Pow latency before and after the final-square removal</text>',
        f'<text x="60" y="69" font-size="13" class="muted">Speedup = baseline ÷ candidate · JMH {html.escape(jmh_version)} · JVM {html.escape(jvm_version)} · generated {html.escape(generated_at)}</text>',
        '<circle cx="70" cy="100" r="5" fill="#172033"/><text x="84" y="104" font-size="12">Non-overlapping 99.9% JMH intervals</text>',
        '<circle cx="325" cy="100" r="5" fill="#fff" stroke="#172033" stroke-width="2"/><text x="339" y="104" font-size="12">Intervals overlap; result inconclusive</text>',
    ]

    for panel_index, (method, label, color) in enumerate(OPERATIONS):
        left = panel_lefts[panel_index]
        plot_left = left + 55
        plot_right = left + panel_width - 12
        plot_height = plot_bottom - plot_top
        parts.append(f'<text x="{left}" y="130" font-size="16" font-weight="700">{html.escape(label)}</text>')

        tick = 1.0
        while tick <= y_max + 0.001:
            y = plot_bottom - (tick / y_max) * plot_height
            parts.append(f'<line x1="{plot_left}" y1="{y:.1f}" x2="{plot_right}" y2="{y:.1f}" class="grid"/>')
            parts.append(f'<text x="{plot_left - 10}" y="{y + 4:.1f}" text-anchor="end" font-size="11">{tick:.1f}×</text>')
            tick += 0.5

        baseline_y = plot_bottom - (1.0 / y_max) * plot_height
        parts.append(f'<line x1="{plot_left}" y1="{baseline_y:.1f}" x2="{plot_right}" y2="{baseline_y:.1f}" stroke="#64748b" stroke-width="1.5" stroke-dasharray="5 4"/>')
        parts.append(f'<line x1="{plot_left}" y1="{plot_top}" x2="{plot_left}" y2="{plot_bottom}" class="axis"/>')
        parts.append(f'<line x1="{plot_left}" y1="{plot_bottom}" x2="{plot_right}" y2="{plot_bottom}" class="axis"/>')

        values = [row for row in rows if row["method"] == method]
        points: list[tuple[float, float]] = []
        for index, row in enumerate(values):
            x = plot_left + (index / (len(BITS) - 1)) * (plot_right - plot_left)
            speedup = float(row["speedup"])
            y = plot_bottom - (speedup / y_max) * plot_height
            points.append((x, y))
            parts.append(f'<text x="{x:.1f}" y="{plot_bottom + 20}" text-anchor="middle" font-size="11">{row["bits"]}</text>')
            fill = color if row["intervals_separated"] else "#ffffff"
            parts.append(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="5" fill="{fill}" stroke="{color}" stroke-width="2"/>')
            parts.append(f'<text x="{x:.1f}" y="{y - 10:.1f}" text-anchor="middle" font-size="11" font-weight="700">{speedup:.2f}×</text>')

        path = " ".join(f"{'M' if index == 0 else 'L'} {x:.1f} {y:.1f}" for index, (x, y) in enumerate(points))
        parts.append(f'<path d="{path}" fill="none" stroke="{color}" stroke-width="2" opacity="0.75"/>')
        parts.append(f'<text x="{(plot_left + plot_right) / 2:.1f}" y="{plot_bottom + 43}" text-anchor="middle" font-size="11" class="muted">Operand size (bits)</text>')

    parts.append('<text x="60" y="535" font-size="11" class="muted">Intervals are separate measurements, not paired confidence intervals. Full scores and intervals are in pow-optimization.csv.</text>')
    parts.append('</svg>')
    return "\n".join(parts) + "\n"


def load_node_results(path: Path) -> list[dict[str, object]]:
    root = ET.parse(path).getroot()
    output = root.findtext("system-out", default="")
    pattern = re.compile(
        r"POW_BENCHMARK bits=(\d+) iterations=(\d+) samples=(\d+) "
        r"runtime=(\S+) platform=(\S+) optimizedNs=([0-9.]+) legacyNs=([0-9.]+)",
    )
    rows: list[dict[str, object]] = []
    for line in output.splitlines():
        match = pattern.search(line)
        if not match:
            continue
        bits, iterations, samples = (int(match.group(index)) for index in (1, 2, 3))
        optimized_ns, legacy_ns = (float(match.group(index)) for index in (6, 7))
        if bits not in (512, 1024, 2048, 4096) or optimized_ns <= 0 or legacy_ns <= 0:
            raise ValueError(f"Invalid Node.js pow result: {line}")
        rows.append(
            {
                "target": "Kotlin/JS Node.js",
                "bits": bits,
                "optimized_ns_per_op": optimized_ns,
                "legacy_ns_per_op": legacy_ns,
                "speedup": legacy_ns / optimized_ns,
                "iterations_per_sample": iterations,
                "sample_count": samples,
                "runtime": match.group(4),
                "platform": match.group(5),
            },
        )
    if sorted(row["bits"] for row in rows) != [512, 1024, 2048, 4096]:
        raise ValueError("Node.js test report must contain one pow measurement for 512, 1024, 2048, and 4096 bits")
    return rows


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("before_json", type=Path, help="Baseline JMH JSON")
    parser.add_argument("after_json", type=Path, help="Candidate JMH JSON")
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[1])
    parser.add_argument("--node-test-xml", type=Path, help="Optional XML output from the opt-in Node.js pow benchmark")
    args = parser.parse_args()

    before, before_jmh, before_jvm = load_results(args.before_json)
    after, after_jmh, after_jvm = load_results(args.after_json)
    if (before_jmh, before_jvm) != (after_jmh, after_jvm):
        raise ValueError("Baseline and candidate must use the same JMH and JVM versions")

    generated_at = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M UTC")
    rows: list[dict[str, object]] = []
    for method, label, _ in OPERATIONS:
        for bits in BITS:
            old = before[method][bits]
            new = after[method][bits]
            rows.append(
                {
                    "method": method,
                    "operation": label,
                    "bits": bits,
                    "before_ns": old["score"],
                    "before_ci_low_ns": old["lower"],
                    "before_ci_high_ns": old["upper"],
                    "after_ns": new["score"],
                    "after_ci_low_ns": new["lower"],
                    "after_ci_high_ns": new["upper"],
                    "speedup": old["score"] / new["score"],
                    "improvement_percent": (1 - new["score"] / old["score"]) * 100,
                    "intervals_separated": old["upper"] < new["lower"] or new["upper"] < old["lower"],
                    "generated_utc": generated_at,
                    "jmh_version": before_jmh,
                    "jvm_version": before_jvm,
                },
            )

    results_path = args.root / "benchmarks/results/pow-optimization.csv"
    results_path.parent.mkdir(parents=True, exist_ok=True)
    with results_path.open("w", encoding="utf-8", newline="") as output:
        writer = csv.DictWriter(output, fieldnames=list(rows[0]))
        writer.writeheader()
        writer.writerows(rows)

    chart_path = args.root / "docs/assets/benchmark-pow-optimization.svg"
    chart_path.parent.mkdir(parents=True, exist_ok=True)
    chart_path.write_text(render_svg(rows, generated_at, before_jmh, before_jvm), encoding="utf-8")
    print(f"Wrote {results_path}")
    print(f"Wrote {chart_path}")

    if args.node_test_xml:
        node_rows = load_node_results(args.node_test_xml)
        node_path = args.root / "benchmarks/results/pow-nodejs.csv"
        with node_path.open("w", encoding="utf-8", newline="") as output:
            writer = csv.DictWriter(output, fieldnames=list(node_rows[0]))
            writer.writeheader()
            writer.writerows(node_rows)
        print(f"Wrote {node_path}")


if __name__ == "__main__":
    main()
