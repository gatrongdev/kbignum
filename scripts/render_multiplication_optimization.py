#!/usr/bin/env python3
"""Render a repeated JMH baseline/candidate comparison for multiplication."""

from __future__ import annotations

import argparse
import csv
import html
import json
import platform
from datetime import datetime, timezone
from pathlib import Path
from typing import Any


INTEGER_SIZES = (1024, 2048, 2560, 3072, 4096, 8192)
DECIMAL_SIZES = (500, 1000)
CSV_FIELDS = (
    "operation",
    "input_size",
    "unit",
    "baseline_mean_ns_op",
    "baseline_min_ns_op",
    "baseline_max_ns_op",
    "optimized_mean_ns_op",
    "optimized_min_ns_op",
    "optimized_max_ns_op",
    "latency_reduction_percent",
    "run_spread_low_percent",
    "run_spread_high_percent",
    "baseline_run_count",
    "optimized_run_count",
    "jmh_version",
    "jvm_version",
    "platform",
    "generated_utc",
)
SUMMARY_START = "<!-- MULTIPLICATION_OPTIMIZATION_START -->"
SUMMARY_END = "<!-- MULTIPLICATION_OPTIMIZATION_END -->"


def read_runs(paths: list[Path], method: str, size_parameter: str, expected_sizes: tuple[int, ...]) -> dict[int, list[dict[str, Any]]]:
    runs = {size: [] for size in expected_sizes}
    for path in paths:
        entries = json.loads(path.read_text(encoding="utf-8"))
        found: set[int] = set()
        for entry in entries:
            if str(entry.get("benchmark", "")).rsplit(".", 1)[-1] != method:
                continue
            params = entry.get("params", {})
            size = int(params[size_parameter])
            if size not in runs or size in found:
                raise ValueError(f"Unexpected or duplicate {method} size {size} in {path}")
            score = float(entry["primaryMetric"]["score"])
            runs[size].append({
                "score": score,
                "jmh_version": str(entry.get("jmhVersion", "unknown")),
                "jvm_version": str(entry.get("vmVersion", "unknown")),
            })
            found.add(size)
        if found != set(expected_sizes):
            raise ValueError(f"{path} is missing {method} results for {sorted(set(expected_sizes) - found)}")
    if any(not values for values in runs.values()):
        raise ValueError(f"No {method} measurements found in the supplied runs")
    return runs


def aggregate_rows(
    operation: str,
    unit: str,
    expected_sizes: tuple[int, ...],
    baseline: dict[int, list[dict[str, Any]]],
    optimized: dict[int, list[dict[str, Any]]],
    generated_utc: str,
) -> list[dict[str, Any]]:
    rows = []
    machine = f"{platform.system()} {platform.release()} / {platform.machine()}"
    for size in expected_sizes:
        baseline_values = [run["score"] for run in baseline[size]]
        optimized_values = [run["score"] for run in optimized[size]]
        baseline_mean = sum(baseline_values) / len(baseline_values)
        optimized_mean = sum(optimized_values) / len(optimized_values)
        reduction = (baseline_mean - optimized_mean) / baseline_mean * 100
        spread_low = (min(baseline_values) - max(optimized_values)) / min(baseline_values) * 100
        spread_high = (max(baseline_values) - min(optimized_values)) / max(baseline_values) * 100
        metadata = optimized[size][0]
        rows.append({
            "operation": operation,
            "input_size": size,
            "unit": unit,
            "baseline_mean_ns_op": baseline_mean,
            "baseline_min_ns_op": min(baseline_values),
            "baseline_max_ns_op": max(baseline_values),
            "optimized_mean_ns_op": optimized_mean,
            "optimized_min_ns_op": min(optimized_values),
            "optimized_max_ns_op": max(optimized_values),
            "latency_reduction_percent": reduction,
            "run_spread_low_percent": spread_low,
            "run_spread_high_percent": spread_high,
            "baseline_run_count": len(baseline_values),
            "optimized_run_count": len(optimized_values),
            "jmh_version": metadata["jmh_version"],
            "jvm_version": metadata["jvm_version"],
            "platform": machine,
            "generated_utc": generated_utc,
        })
    return rows


def render_chart(rows: list[dict[str, Any]], generated_utc: str) -> str:
    width = 1120
    height = 540
    plot_left = 270
    plot_width = 610
    maximum = 40
    integer_rows = [row for row in rows if row["operation"] == "KBigInteger multiply"]
    decimal_rows = [row for row in rows if row["operation"] == "KBigDecimal multiply"]
    pieces = [
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}" role="img" aria-labelledby="title description">',
        '<title id="title">KBigNum multiplication latency reduction</title>',
        '<desc id="description">Average JMH latency reduction for optimized multiplication compared with repeated baseline runs. Whiskers show the spread across measured runs.</desc>',
        '<rect width="100%" height="100%" fill="#ffffff"/>',
        '<style>text{font-family:Arial,Helvetica,sans-serif;fill:#172033}.muted{fill:#64748b}.small{font-size:12px}.label{font-size:14px}.title{font-size:23px;font-weight:700}.group{font-size:16px;font-weight:700}.grid{stroke:#e2e8f0;stroke-width:1}.target{stroke-dasharray:5 5;stroke-width:2}</style>',
        '<text x="32" y="38" class="title">Multiplication latency reduction</text>',
        '<text x="32" y="62" class="small muted">Mean of repeated JMH runs · whiskers show run-to-run spread, not a formal confidence interval</text>',
    ]

    for tick in range(0, maximum + 1, 10):
        x = plot_left + plot_width * tick / maximum
        pieces.append(f'<line x1="{x:.1f}" y1="88" x2="{x:.1f}" y2="468" class="grid"/>')
        pieces.append(f'<text x="{x:.1f}" y="489" text-anchor="middle" class="small muted">{tick}%</text>')
    for target, color, label in ((20, "#2563eb", "integer target 20%"), (15, "#ea580c", "decimal target 15%")):
        x = plot_left + plot_width * target / maximum
        y = 80 if target == 20 else 102
        pieces.append(f'<line x1="{x:.1f}" y1="88" x2="{x:.1f}" y2="468" stroke="{color}" class="target" opacity="0.65"/>')
        pieces.append(f'<text x="{x + 5:.1f}" y="{y}" class="small" fill="{color}">{html.escape(label)}</text>')

    pieces.append('<text x="32" y="112" class="group">KBigInteger · input bits</text>')
    y = 145
    for row in integer_rows:
        y = add_bar(pieces, row, y, plot_left, plot_width, maximum, "#2563eb", 20, f"{row['input_size']} bits")
        y += 8
    pieces.append('<text x="32" y="386" class="group">KBigDecimal · input digits</text>')
    y = 415
    for row in decimal_rows:
        y = add_bar(pieces, row, y, plot_left, plot_width, maximum, "#ea580c", 15, f"{row['input_size']} digits")
        y += 8
    pieces.append('<text x="32" y="520" class="small muted">Positive values mean lower latency. Baseline and optimized numbers are arithmetic means across the listed runs.</text>')
    pieces.append(f'<text x="1088" y="520" text-anchor="end" class="small muted">{html.escape(generated_utc)}</text>')
    pieces.append('</svg>')
    return "\n".join(pieces) + "\n"


def add_bar(pieces: list[str], row: dict[str, Any], y: int, left: int, plot_width: int, maximum: int, color: str, target: int, label: str) -> int:
    reduction = row["latency_reduction_percent"]
    min_spread = row["run_spread_low_percent"]
    max_spread = row["run_spread_high_percent"]
    x0 = left
    x1 = left + plot_width * max(0, min(maximum, reduction)) / maximum
    whisker0 = left + plot_width * max(0, min(maximum, min_spread)) / maximum
    whisker1 = left + plot_width * max(0, min(maximum, max_spread)) / maximum
    fill = color if reduction >= target else "#94a3b8"
    pieces.append(f'<text x="32" y="{y + 5}" class="label">{html.escape(label)}</text>')
    pieces.append(f'<rect x="{x0}" y="{y - 9}" width="{max(1, x1 - x0):.1f}" height="18" rx="4" fill="{fill}"/>')
    pieces.append(f'<line x1="{whisker0:.1f}" y1="{y}" x2="{whisker1:.1f}" y2="{y}" stroke="#0f172a" stroke-width="2"/>')
    pieces.append(f'<line x1="{whisker0:.1f}" y1="{y - 5}" x2="{whisker0:.1f}" y2="{y + 5}" stroke="#0f172a"/>')
    pieces.append(f'<line x1="{whisker1:.1f}" y1="{y - 5}" x2="{whisker1:.1f}" y2="{y + 5}" stroke="#0f172a"/>')
    pieces.append(f'<text x="900" y="{y + 5}" class="label">{reduction:.1f}%</text>')
    pieces.append(f'<text x="970" y="{y + 5}" class="small muted">{row["baseline_mean_ns_op"]:.0f} → {row["optimized_mean_ns_op"]:.0f} ns/op</text>')
    return y


def render_summary(rows: list[dict[str, Any]]) -> str:
    integer_rows = [row for row in rows if row["operation"] == "KBigInteger multiply"]
    decimal_rows = [row for row in rows if row["operation"] == "KBigDecimal multiply"]
    integer_meeting = sum(row["latency_reduction_percent"] >= 20 for row in integer_rows)
    decimal_meeting = sum(row["latency_reduction_percent"] >= 15 for row in decimal_rows)
    lines = [
        "### Multiplication optimization A/B profile",
        "",
        "The chart compares repeated baseline and optimized JMH runs on the same JVM and machine. Bars show mean latency reduction; whiskers combine the observed run-to-run ranges and are not formal confidence intervals.",
        "",
        "![Repeated JMH multiplication latency reduction](docs/assets/benchmark-multiplication-optimization.svg)",
        "",
        "| Workload | Baseline mean | Optimized mean | Latency reduction |",
        "| :--- | ---: | ---: | ---: |",
    ]
    for row in integer_rows + decimal_rows:
        unit = "bits" if row["unit"] == "bits" else "digits"
        lines.append(
            f"| {row['operation']} · {row['input_size']} {unit} | {row['baseline_mean_ns_op']:.0f} ns/op | {row['optimized_mean_ns_op']:.0f} ns/op | {row['latency_reduction_percent']:.1f}% |",
        )
    lines.extend([
        "",
        f"The measured target was reached at {integer_meeting}/{len(integer_rows)} KBigInteger sizes (at least 20% reduction) and {decimal_meeting}/{len(decimal_rows)} KBigDecimal sizes (at least 15% reduction). The aggregated comparison and environment details are in [`multiplication-optimization.csv`](benchmarks/results/multiplication-optimization.csv).",
    ])
    return "\n".join(lines)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--integer-baseline", action="append", type=Path, required=True)
    parser.add_argument("--integer-optimized", action="append", type=Path, required=True)
    parser.add_argument("--decimal-baseline", action="append", type=Path, required=True)
    parser.add_argument("--decimal-optimized", action="append", type=Path, required=True)
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[1])
    args = parser.parse_args()

    generated_utc = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M UTC")
    integer_baseline = read_runs(args.integer_baseline, "integerMultiplyKBigNum", "bits", INTEGER_SIZES)
    integer_optimized = read_runs(args.integer_optimized, "integerMultiplyKBigNum", "bits", INTEGER_SIZES)
    decimal_baseline = read_runs(args.decimal_baseline, "decimalMultiplyKBigNum", "digits", DECIMAL_SIZES)
    decimal_optimized = read_runs(args.decimal_optimized, "decimalMultiplyKBigNum", "digits", DECIMAL_SIZES)
    rows = aggregate_rows(
        "KBigInteger multiply",
        "bits",
        INTEGER_SIZES,
        integer_baseline,
        integer_optimized,
        generated_utc,
    ) + aggregate_rows(
        "KBigDecimal multiply",
        "digits",
        DECIMAL_SIZES,
        decimal_baseline,
        decimal_optimized,
        generated_utc,
    )

    root = args.root.resolve()
    csv_path = root / "benchmarks/results/multiplication-optimization.csv"
    csv_path.parent.mkdir(parents=True, exist_ok=True)
    with csv_path.open("w", newline="", encoding="utf-8") as stream:
        writer = csv.DictWriter(stream, fieldnames=CSV_FIELDS, lineterminator="\n")
        writer.writeheader()
        writer.writerows(rows)

    chart_path = root / "docs/assets/benchmark-multiplication-optimization.svg"
    chart_path.parent.mkdir(parents=True, exist_ok=True)
    chart_path.write_text(render_chart(rows, generated_utc), encoding="utf-8")

    readme_path = root / "README.md"
    readme = readme_path.read_text(encoding="utf-8")
    if readme.count(SUMMARY_START) != 1 or readme.count(SUMMARY_END) != 1:
        raise ValueError("README must contain exactly one multiplication optimization marker pair")
    start = readme.index(SUMMARY_START) + len(SUMMARY_START)
    end = readme.index(SUMMARY_END)
    if end < start:
        raise ValueError("README multiplication optimization markers are out of order")
    readme_path.write_text(readme[:start] + "\n\n" + render_summary(rows) + "\n\n" + readme[end:], encoding="utf-8")
    print(f"Updated {csv_path.relative_to(root)} and {chart_path.relative_to(root)}")


if __name__ == "__main__":
    main()
