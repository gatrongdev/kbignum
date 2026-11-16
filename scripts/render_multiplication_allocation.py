#!/usr/bin/env python3
"""Summarize JMH allocation profiles and paired Kotlin/JS multiplication runs."""

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
from typing import Any


CSV_FIELDS = (
    "runtime",
    "runtime_version",
    "platform",
    "operation",
    "input_size",
    "input_unit",
    "comparison_pair",
    "baseline_latency_ns_op",
    "baseline_latency_ci99_9_lower_ns_op",
    "baseline_latency_ci99_9_upper_ns_op",
    "optimized_latency_ns_op",
    "optimized_latency_ci99_9_lower_ns_op",
    "optimized_latency_ci99_9_upper_ns_op",
    "latency_reduction_percent",
    "baseline_allocation_b_op",
    "optimized_allocation_b_op",
    "allocation_reduction_percent",
    "forks",
    "warmup_iterations",
    "measurement_iterations",
    "operations_per_node_sample",
    "node_samples",
    "generated_utc",
)
NODE_RECORD = re.compile(
    r"DECIMAL_MULTIPLY_BALANCED_BENCHMARK digits=(?P<size>\d+) "
    r"iterations=(?P<operations>\d+) samples=(?P<samples>\d+) "
    r"runtime=(?P<runtime>\S+) platform=(?P<platform>\S+) "
    r"medianNs=(?P<score>[\d.]+)"
)


def read_jmh(path: Path, method: str, size_parameter: str) -> dict[int, dict[str, Any]]:
    results: dict[int, dict[str, Any]] = {}
    for entry in json.loads(path.read_text(encoding="utf-8")):
        if str(entry.get("benchmark", "")).rsplit(".", 1)[-1] != method:
            continue
        size = int(entry["params"][size_parameter])
        if size in results:
            raise ValueError(f"Duplicate {method} measurement for {size} in {path}")
        allocation = entry.get("secondaryMetrics", {}).get("·gc.alloc.rate.norm")
        if allocation is None:
            raise ValueError(f"Missing -prof gc allocation result for {method} in {path}")
        results[size] = {
            "latency": float(entry["primaryMetric"]["score"]),
            "latency_ci": [float(value) for value in entry["primaryMetric"]["scoreConfidence"]],
            "allocation": float(allocation["score"]),
            "runtime_version": f"JDK {entry.get('jdkVersion', 'unknown')} / {entry.get('vmVersion', 'unknown')}",
            "platform": f"{platform.system()} {platform.release()} / {platform.machine()}",
            "forks": int(entry["forks"]),
            "warmup_iterations": int(entry["warmupIterations"]),
            "measurement_iterations": int(entry["measurementIterations"]),
        }
    if not results:
        raise ValueError(f"No {method} measurements found in {path}")
    return results


def read_node(path: Path) -> dict[int, dict[str, Any]]:
    root = ET.parse(path).getroot()
    output = root.findtext("system-out") or ""
    results: dict[int, dict[str, Any]] = {}
    for match in NODE_RECORD.finditer(output):
        size = int(match["size"])
        if size in results:
            raise ValueError(f"Duplicate Node.js measurement for {size} digits in {path}")
        results[size] = {
            "latency": float(match["score"]),
            "runtime_version": f"Node.js {match['runtime']}",
            "platform": match["platform"],
            "operations": int(match["operations"]),
            "samples": int(match["samples"]),
        }
    if not results:
        raise ValueError(f"No balanced decimal multiplication measurements found in {path}")
    return results


def reduction(baseline: float, optimized: float) -> float:
    return (baseline - optimized) / baseline * 100


def jmh_rows(
    operation: str,
    unit: str,
    method: str,
    parameter: str,
    baseline_path: Path,
    optimized_path: Path,
    generated_utc: str,
) -> list[dict[str, Any]]:
    baseline = read_jmh(baseline_path, method, parameter)
    optimized = read_jmh(optimized_path, method, parameter)
    if baseline.keys() != optimized.keys():
        raise ValueError(f"Baseline and optimized JMH sizes differ for {operation}")
    rows = []
    for size in sorted(baseline):
        before = baseline[size]
        after = optimized[size]
        rows.append({
            "runtime": "JVM",
            "runtime_version": after["runtime_version"],
            "platform": after["platform"],
            "operation": operation,
            "input_size": size,
            "input_unit": unit,
            "comparison_pair": "baseline vs optimized",
            "baseline_latency_ns_op": before["latency"],
            "baseline_latency_ci99_9_lower_ns_op": before["latency_ci"][0],
            "baseline_latency_ci99_9_upper_ns_op": before["latency_ci"][1],
            "optimized_latency_ns_op": after["latency"],
            "optimized_latency_ci99_9_lower_ns_op": after["latency_ci"][0],
            "optimized_latency_ci99_9_upper_ns_op": after["latency_ci"][1],
            "latency_reduction_percent": reduction(before["latency"], after["latency"]),
            "baseline_allocation_b_op": before["allocation"],
            "optimized_allocation_b_op": after["allocation"],
            "allocation_reduction_percent": reduction(before["allocation"], after["allocation"]),
            "forks": after["forks"],
            "warmup_iterations": after["warmup_iterations"],
            "measurement_iterations": after["measurement_iterations"],
            "operations_per_node_sample": "",
            "node_samples": "",
            "generated_utc": generated_utc,
        })
    return rows


def node_rows(baseline_paths: list[Path], optimized_paths: list[Path], generated_utc: str) -> list[dict[str, Any]]:
    if len(baseline_paths) != len(optimized_paths) or not baseline_paths:
        raise ValueError("Pass the same non-zero number of --node-baseline and --node-optimized files")
    rows = []
    for pair, (baseline_path, optimized_path) in enumerate(zip(baseline_paths, optimized_paths), start=1):
        baseline = read_node(baseline_path)
        optimized = read_node(optimized_path)
        if baseline.keys() != optimized.keys():
            raise ValueError(f"Baseline and optimized Node.js sizes differ in pair {pair}")
        for size in sorted(baseline):
            before = baseline[size]
            after = optimized[size]
            rows.append({
                "runtime": "Kotlin/JS on Node.js",
                "runtime_version": after["runtime_version"],
                "platform": after["platform"],
                "operation": "KBigDecimal multiply",
                "input_size": size,
                "input_unit": "digits",
                "comparison_pair": pair,
                "baseline_latency_ns_op": before["latency"],
                "baseline_latency_ci99_9_lower_ns_op": "",
                "baseline_latency_ci99_9_upper_ns_op": "",
                "optimized_latency_ns_op": after["latency"],
                "optimized_latency_ci99_9_lower_ns_op": "",
                "optimized_latency_ci99_9_upper_ns_op": "",
                "latency_reduction_percent": reduction(before["latency"], after["latency"]),
                "baseline_allocation_b_op": "",
                "optimized_allocation_b_op": "",
                "allocation_reduction_percent": "",
                "forks": "",
                "warmup_iterations": "",
                "measurement_iterations": "",
                "operations_per_node_sample": after["operations"],
                "node_samples": after["samples"],
                "generated_utc": generated_utc,
            })
    return rows


def render_chart(rows: list[dict[str, Any]], generated_utc: str) -> str:
    jvm_rows = [row for row in rows if row["runtime"] == "JVM"]
    integer_rows = [row for row in jvm_rows if row["operation"] == "KBigInteger multiply"]
    decimal_rows = [row for row in jvm_rows if row["operation"] == "KBigDecimal multiply"]
    width, height = 1000, 455
    left, plot_width, maximum = 250, 440, 40
    parts = [
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}" role="img" aria-labelledby="title description">',
        '<title id="title">Multiplication allocation reduction</title>',
        '<desc id="description">JVM JMH garbage-collection profiler allocation per operation before and after multiplication result ownership optimization.</desc>',
        '<rect width="100%" height="100%" fill="#ffffff"/>',
        '<style>text{font-family:Arial,Helvetica,sans-serif;fill:#172033}.muted{fill:#64748b}.small{font-size:12px}.label{font-size:14px}.title{font-size:23px;font-weight:700}.group{font-size:16px;font-weight:700}.grid{stroke:#e2e8f0;stroke-width:1}</style>',
        '<text x="30" y="36" class="title">Multiplication allocation reduction</text>',
        '<text x="30" y="60" class="small muted">JVM JMH · -prof gc · lower B/op is better; time intervals overlap for every measured size</text>',
    ]
    for tick in range(0, maximum + 1, 10):
        x = left + plot_width * tick / maximum
        parts.append(f'<line x1="{x:.1f}" y1="85" x2="{x:.1f}" y2="374" class="grid"/>')
        parts.append(f'<text x="{x:.1f}" y="395" text-anchor="middle" class="small muted">{tick}%</text>')
    parts.append('<text x="30" y="102" class="group">KBigInteger · input bits</text>')
    y = 132
    for row in integer_rows:
        y = add_bar(parts, row, y, left, plot_width, maximum, "#2563eb", f"{row['input_size']} bits")
        y += 26
    parts.append('<text x="30" y="292" class="group">KBigDecimal · input digits</text>')
    y = 322
    for row in decimal_rows:
        y = add_bar(parts, row, y, left, plot_width, maximum, "#ea580c", f"{row['input_size']} digits")
        y += 26
    parts.append('<text x="30" y="433" class="small muted">Allocation is a stable JVM measurement; this run does not establish a latency gain.</text>')
    parts.append(f'<text x="970" y="433" text-anchor="end" class="small muted">{html.escape(generated_utc)}</text>')
    parts.append('</svg>')
    return "\n".join(parts) + "\n"


def add_bar(parts: list[str], row: dict[str, Any], y: int, left: int, plot_width: int, maximum: int, color: str, label: str) -> int:
    percent = float(row["allocation_reduction_percent"])
    x1 = left + plot_width * max(0, min(maximum, percent)) / maximum
    parts.append(f'<text x="30" y="{y + 5}" class="label">{html.escape(label)}</text>')
    parts.append(f'<rect x="{left}" y="{y - 9}" width="{max(1, x1 - left):.1f}" height="18" rx="4" fill="{color}"/>')
    parts.append(f'<text x="{left + plot_width + 15}" y="{y + 5}" class="label">{percent:.1f}%</text>')
    parts.append(
        f'<text x="{left + plot_width + 82}" y="{y + 5}" class="small muted">'
        f'{row["baseline_allocation_b_op"]:.0f} → {row["optimized_allocation_b_op"]:.0f} B/op</text>'
    )
    return y


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--integer-baseline", required=True, type=Path)
    parser.add_argument("--integer-optimized", required=True, type=Path)
    parser.add_argument("--decimal-baseline", required=True, type=Path)
    parser.add_argument("--decimal-optimized", required=True, type=Path)
    parser.add_argument("--node-baseline", action="append", type=Path, default=[])
    parser.add_argument("--node-optimized", action="append", type=Path, default=[])
    parser.add_argument("--csv", type=Path, default=Path("benchmarks/results/multiplication-allocation.csv"))
    parser.add_argument("--svg", type=Path, default=Path("docs/assets/benchmark-multiplication-allocation.svg"))
    args = parser.parse_args()

    generated_utc = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M UTC")
    rows = []
    rows.extend(jmh_rows(
        "KBigInteger multiply", "bits", "integerMultiplyKBigNum", "bits",
        args.integer_baseline, args.integer_optimized, generated_utc,
    ))
    rows.extend(jmh_rows(
        "KBigDecimal multiply", "digits", "decimalMultiplyKBigNum", "digits",
        args.decimal_baseline, args.decimal_optimized, generated_utc,
    ))
    rows.extend(node_rows(args.node_baseline, args.node_optimized, generated_utc))

    args.csv.parent.mkdir(parents=True, exist_ok=True)
    with args.csv.open("w", encoding="utf-8", newline="") as output:
        writer = csv.DictWriter(output, fieldnames=CSV_FIELDS)
        writer.writeheader()
        writer.writerows(rows)
    args.svg.parent.mkdir(parents=True, exist_ok=True)
    args.svg.write_text(render_chart(rows, generated_utc), encoding="utf-8")
    print(f"Wrote {args.csv} and {args.svg}")


if __name__ == "__main__":
    main()
