#!/usr/bin/env python3
"""Turn KBigDecimal JMH results into a tracked CSV, SVG chart, and README table."""

from __future__ import annotations

import argparse
import csv
import html
import json
import math
import platform
from datetime import datetime, timezone
from pathlib import Path
from typing import Dict


EXPECTED_DIGITS = [34, 100, 500, 1000]
OPERATIONS = {
    "decimalAdd": "Addition (scale 8)",
    "decimalAddMismatched": "Addition (scales 8 and 16)",
    "decimalSubtract": "Subtraction (scale 8)",
    "decimalSubtractMismatched": "Subtraction (scales 8 and 16)",
    "decimalMultiply": "Multiplication (scales 8 and 16)",
    "decimalDivide": "Division (result scale 16, HalfUp)",
    "decimalSetScale": "setScale 8 to 4 (HalfUp)",
    "decimalParse": "Parsing",
    "decimalFormat": "Plain-string formatting",
}
IMPLEMENTATIONS = {
    "Java": "Java BigDecimal",
    "KBigNum": "KBigDecimal",
}
COLORS = {
    "Java": "#2563eb",
    "KBigNum": "#dc2626",
}
SUMMARY_START = "<!-- BIGDECIMAL_BENCHMARK_SUMMARY_START -->"
SUMMARY_END = "<!-- BIGDECIMAL_BENCHMARK_SUMMARY_END -->"


Measurement = Dict[str, float]
DecimalResults = Dict[str, Dict[str, Dict[int, Measurement]]]


def parse_results(path: Path) -> tuple[DecimalResults, str, str]:
    entries = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(entries, list) or not entries:
        raise ValueError("JMH JSON must contain a non-empty result array")

    results: DecimalResults = {
        operation: {implementation: {} for implementation in IMPLEMENTATIONS}
        for operation in OPERATIONS.values()
    }
    jvm_version = str(entries[0].get("vmVersion", "unknown"))
    jmh_version = str(entries[0].get("jmhVersion", "unknown"))

    for entry in entries:
        method = str(entry.get("benchmark", "")).rsplit(".", 1)[-1]
        implementation = next((key for key in IMPLEMENTATIONS if method.endswith(key)), None)
        if implementation is None:
            continue

        operation_key = method[: -len(implementation)]
        if operation_key not in OPERATIONS:
            continue

        try:
            digits = int(entry["params"]["digits"])
            metric = entry["primaryMetric"]
            score = float(metric["score"])
            confidence = metric.get("scoreConfidence", [score, score])
            lower = float(confidence[0]) if len(confidence) > 0 else score
            upper = float(confidence[1]) if len(confidence) > 1 else score
        except (KeyError, TypeError, ValueError, IndexError) as error:
            raise ValueError(f"Malformed JMH result for {method}: {error}") from error

        if (
            digits not in EXPECTED_DIGITS
            or not all(math.isfinite(value) for value in (score, lower, upper))
            or score <= 0
        ):
            raise ValueError(f"Invalid measurement for {method} at {digits} digits")

        operation = OPERATIONS[operation_key]
        samples = results[operation][implementation]
        if digits in samples:
            raise ValueError(f"Duplicate result for {operation} {implementation} at {digits} digits")
        samples[digits] = {"score": score, "lower": lower, "upper": upper}

    missing = [
        f"{operation}/{implementation}/{digits}"
        for operation, implementations in results.items()
        for implementation, samples in implementations.items()
        for digits in EXPECTED_DIGITS
        if digits not in samples
    ]
    if missing:
        raise ValueError("Incomplete decimal benchmark run; missing: " + ", ".join(missing))

    return results, jvm_version, jmh_version


def render_svg(results: DecimalResults, generated_utc: str, jvm_version: str, jmh_version: str) -> str:
    width = 1200
    panel_width = 550
    panel_height = 235
    panel_x = [55, 625]
    panel_gap_y = 265
    first_panel_y = 120
    rows = math.ceil(len(OPERATIONS) / 2)
    height = first_panel_y + (rows - 1) * panel_gap_y + panel_height + 55
    parts = [
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}">',
        '<rect width="100%" height="100%" fill="#ffffff"/>',
        '<style>text{font-family:Arial,Helvetica,sans-serif;fill:#172033}.muted{fill:#5b6474}.grid{stroke:#d8dee8;stroke-width:1}.axis{stroke:#64748b;stroke-width:1}</style>',
        '<text x="55" y="42" font-size="26" font-weight="700">KBigDecimal JVM latency by input precision</text>',
        f'<text x="55" y="68" font-size="13" class="muted">Average time (ns/op) · JMH {html.escape(jmh_version)} · JVM {html.escape(jvm_version)} · generated {html.escape(generated_utc)}</text>',
        '<line x1="790" y1="96" x2="820" y2="96" stroke="#2563eb" stroke-width="3"/><circle cx="805" cy="96" r="4" fill="#2563eb"/><text x="828" y="100" font-size="13">Java BigDecimal</text>',
        '<line x1="970" y1="96" x2="1000" y2="96" stroke="#dc2626" stroke-width="3"/><circle cx="985" cy="96" r="4" fill="#dc2626"/><text x="1008" y="100" font-size="13">KBigDecimal</text>',
    ]

    for operation_index, operation in enumerate(OPERATIONS.values()):
        x = panel_x[operation_index % 2]
        y = first_panel_y + (operation_index // 2) * panel_gap_y
        plot_left = x + 62
        plot_right = x + panel_width - 14
        plot_top = y + 34
        plot_bottom = y + panel_height - 43
        plot_width = plot_right - plot_left
        plot_height = plot_bottom - plot_top
        maximum = max(
            results[operation][implementation][digits]["upper"]
            for implementation in IMPLEMENTATIONS
            for digits in EXPECTED_DIGITS
        )
        y_max = maximum * 1.12 if maximum > 0 else 1.0

        parts.append(f'<rect x="{x}" y="{y}" width="{panel_width}" height="{panel_height}" rx="8" fill="#fff" stroke="#d8dee8"/>')
        parts.append(f'<text x="{x + 14}" y="{y + 22}" font-size="15" font-weight="700">{html.escape(operation)}</text>')

        for tick in range(5):
            value = y_max * tick / 4
            tick_y = plot_bottom - (plot_height * tick / 4)
            parts.append(f'<line x1="{plot_left}" y1="{tick_y:.1f}" x2="{plot_right}" y2="{tick_y:.1f}" class="grid"/>')
            parts.append(f'<text x="{plot_left - 8}" y="{tick_y + 4:.1f}" font-size="10" text-anchor="end" class="muted">{value:.3g}</text>')

        parts.append(f'<line x1="{plot_left}" y1="{plot_top}" x2="{plot_left}" y2="{plot_bottom}" class="axis"/>')
        parts.append(f'<line x1="{plot_left}" y1="{plot_bottom}" x2="{plot_right}" y2="{plot_bottom}" class="axis"/>')

        def x_position(digit_index: int) -> float:
            return plot_left + digit_index / (len(EXPECTED_DIGITS) - 1) * plot_width

        def y_position(value: float) -> float:
            return plot_bottom - (value / y_max) * plot_height

        for digit_index, digits in enumerate(EXPECTED_DIGITS):
            digit_x = x_position(digit_index)
            parts.append(f'<line x1="{digit_x:.1f}" y1="{plot_bottom}" x2="{digit_x:.1f}" y2="{plot_bottom + 4}" class="axis"/>')
            parts.append(f'<text x="{digit_x:.1f}" y="{plot_bottom + 17}" font-size="10" text-anchor="middle" class="muted">{digits}</text>')

        for implementation in IMPLEMENTATIONS:
            color = COLORS[implementation]
            points = []
            for digits_index, digits in enumerate(EXPECTED_DIGITS):
                measurement = results[operation][implementation][digits]
                point_x = x_position(digits_index)
                point_y = y_position(measurement["score"])
                points.append(f"{point_x:.1f},{point_y:.1f}")

                low_y = y_position(max(0.0, measurement["lower"]))
                high_y = y_position(measurement["upper"])
                parts.append(f'<line x1="{point_x:.1f}" y1="{low_y:.1f}" x2="{point_x:.1f}" y2="{high_y:.1f}" stroke="{color}" stroke-width="1.5"/>')
                parts.append(f'<line x1="{point_x - 3:.1f}" y1="{low_y:.1f}" x2="{point_x + 3:.1f}" y2="{low_y:.1f}" stroke="{color}" stroke-width="1.5"/>')
                parts.append(f'<line x1="{point_x - 3:.1f}" y1="{high_y:.1f}" x2="{point_x + 3:.1f}" y2="{high_y:.1f}" stroke="{color}" stroke-width="1.5"/>')
                parts.append(f'<circle cx="{point_x:.1f}" cy="{point_y:.1f}" r="3.5" fill="{color}"/>')

            parts.append(f'<polyline points="{" ".join(points)}" fill="none" stroke="{color}" stroke-width="2.5" stroke-linejoin="round"/>')

        parts.append(f'<text x="{x + 15}" y="{y + panel_height - 10}" font-size="10" class="muted">Input precision (decimal digits)</text>')
        parts.append(f'<text x="{x + panel_width - 9}" y="{y + panel_height - 10}" font-size="10" text-anchor="end" class="muted">lower is faster · JMH CI</text>')

    parts.append(f'<text x="55" y="{height - 12}" font-size="11" class="muted">Seeded signed operands; outputs are checked against java.math.BigDecimal before timing. Division uses scale 16 and HalfUp.</text>')
    parts.append('</svg>')
    return "\n".join(parts) + "\n"


def render_summary(results: DecimalResults, generated_utc: str, jvm_version: str, jmh_version: str) -> str:
    header = "| Operation (KBigDecimal / Java BigDecimal) | " + " | ".join(f"{digits} digits" for digits in EXPECTED_DIGITS) + " |"
    separator = "| :--- | " + " | ".join(":---:" for _ in EXPECTED_DIGITS) + " |"
    rows = [header, separator]

    for operation in OPERATIONS.values():
        cells = []
        for digits in EXPECTED_DIGITS:
            java = results[operation]["Java"][digits]
            kbig = results[operation]["KBigNum"][digits]
            ratio = kbig["score"] / java["score"]
            ratio_text = f"{ratio:.2f}x"
            if kbig["upper"] < java["lower"]:
                cells.append(f"**↓ {ratio_text}**")
            elif kbig["lower"] > java["upper"]:
                cells.append(f"**↑ {ratio_text}**")
            else:
                cells.append(f"{ratio_text}†")
        rows.append(f"| {operation} | " + " | ".join(cells) + " |")

    repeatable_slowdowns = [
        operation
        for operation in OPERATIONS.values()
        if all(
            results[operation]["KBigNum"][digits]["lower"]
            > results[operation]["Java"][digits]["upper"]
            for digits in EXPECTED_DIGITS
        )
    ]
    if repeatable_slowdowns:
        observation = (
            "Across every tested input size in this run, KBigDecimal had separated slower intervals for "
            + ", ".join(repeatable_slowdowns)
            + ". Treat these as profiling candidates for the recorded JVM and machine."
        )
    else:
        observation = "No operation had separated slower KBigDecimal intervals at every tested input size in this run."

    machine = f"{platform.system()} {platform.release()} / {platform.machine()}"
    return "\n".join(
        [
            "### KBigDecimal latency ratio",
            "",
            "Each cell is `KBigDecimal ns/op ÷ Java BigDecimal ns/op`. `↓`/`↑` mark non-overlapping JMH intervals; `†` marks overlapping intervals, so the ratio is inconclusive.",
            "",
            "Interval overlap is a conservative visual cue, not a formal paired significance test.",
            "",
            *rows,
            "",
            observation,
            "",
            f"Generated {generated_utc} on {machine} with JMH {jmh_version} and JVM {jvm_version}. Data: [`latest-bigdecimal.csv`](benchmarks/results/latest-bigdecimal.csv).",
        ],
    )


def write_csv(path: Path, results: DecimalResults, jvm_version: str, jmh_version: str, generated_utc: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", newline="", encoding="utf-8") as output:
        writer = csv.writer(output, lineterminator="\n")
        writer.writerow(
            [
                "operation",
                "implementation",
                "input_digits",
                "score_ns_op",
                "ci_lower_ns_op",
                "ci_upper_ns_op",
                "jmh_version",
                "jvm_version",
                "platform",
                "generated_utc",
            ],
        )
        machine = f"{platform.system()} {platform.release()} / {platform.machine()}"
        for operation in OPERATIONS.values():
            for implementation in IMPLEMENTATIONS:
                for digits in EXPECTED_DIGITS:
                    measurement = results[operation][implementation][digits]
                    writer.writerow(
                        [
                            operation,
                            IMPLEMENTATIONS[implementation],
                            digits,
                            f"{measurement['score']:.6f}",
                            f"{measurement['lower']:.6f}",
                            f"{measurement['upper']:.6f}",
                            jmh_version,
                            jvm_version,
                            machine,
                            generated_utc,
                        ],
                    )


def update_readme(path: Path, summary: str) -> None:
    readme = path.read_text(encoding="utf-8")
    if readme.count(SUMMARY_START) != 1 or readme.count(SUMMARY_END) != 1:
        raise ValueError("README must contain exactly one BigDecimal benchmark summary marker pair")
    start = readme.index(SUMMARY_START) + len(SUMMARY_START)
    end = readme.index(SUMMARY_END)
    if end < start:
        raise ValueError("README BigDecimal benchmark summary markers are out of order")
    path.write_text(readme[:start] + "\n\n" + summary + "\n\n" + readme[end:], encoding="utf-8")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("jmh_json", type=Path, help="JSON output produced by the JMH Gradle task")
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[1])
    args = parser.parse_args()

    results, jvm_version, jmh_version = parse_results(args.jmh_json)
    generated_utc = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M UTC")
    root = args.root.resolve()
    write_csv(root / "benchmarks/results/latest-bigdecimal.csv", results, jvm_version, jmh_version, generated_utc)
    chart_path = root / "docs/assets/benchmark-bigdecimal-latency.svg"
    chart_path.parent.mkdir(parents=True, exist_ok=True)
    chart_path.write_text(render_svg(results, generated_utc, jvm_version, jmh_version), encoding="utf-8")
    update_readme(root / "README.md", render_summary(results, generated_utc, jvm_version, jmh_version))
    print(f"Updated {chart_path.relative_to(root)}, latest-bigdecimal.csv, and README BigDecimal benchmark summary")


if __name__ == "__main__":
    main()
