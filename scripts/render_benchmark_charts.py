#!/usr/bin/env python3
"""Turn a complete JMH JSON run into a tracked CSV, README table, and SVG chart."""

from __future__ import annotations

import argparse
import csv
import html
import json
import math
import platform
import subprocess
import sys
from datetime import datetime, timezone
from pathlib import Path
from typing import Dict


EXPECTED_BITS = [1024, 2048, 2560, 3072, 4096, 8192]
EXPECTED_BASE_WORDS = [64, 72, 80, 88, 96, 112, 128, 160, 256]
OPERAND_RATIOS = [1, 2, 4]
OPERATIONS = {
    "integerAdd": "Addition",
    "integerSubtract": "Subtraction",
    "integerMultiply": "Multiplication",
    "integerDivide": "Division",
    "integerRemainder": "Remainder",
}
IMPLEMENTATIONS = {
    "Java": "Java BigInteger",
    "KBigNum": "KBigInteger",
}
COLORS = {
    "Java": "#2563eb",
    "KBigNum": "#dc2626",
}
CROSSOVER_METHODS = {
    "multiplyProduction": "KBigInteger production path",
    "multiplySchoolbookReference": "Forced schoolbook reference",
}
STRATEGY_COLORS = {
    "multiplyProduction": "#dc2626",
    "multiplySchoolbookReference": "#2563eb",
}
SUMMARY_START = "<!-- BENCHMARK_SUMMARY_START -->"
SUMMARY_END = "<!-- BENCHMARK_SUMMARY_END -->"


Measurement = Dict[str, float]
CoreResults = Dict[str, Dict[str, Dict[int, Measurement]]]
CrossoverResults = Dict[int, Dict[str, Dict[int, Measurement]]]


def parse_results(path: Path) -> tuple[CoreResults, CrossoverResults, str, str]:
    entries = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(entries, list) or not entries:
        raise ValueError("JMH JSON must contain a non-empty result array")

    results: dict[str, dict[str, dict[int, dict[str, float]]]] = {
        operation: {implementation: {} for implementation in IMPLEMENTATIONS}
        for operation in OPERATIONS.values()
    }
    crossover: CrossoverResults = {
        ratio: {method: {} for method in CROSSOVER_METHODS}
        for ratio in OPERAND_RATIOS
    }
    jvm_version = str(entries[0].get("vmVersion", "unknown"))
    jmh_version = str(entries[0].get("jmhVersion", "unknown"))

    for entry in entries:
        method = str(entry.get("benchmark", "")).rsplit(".", 1)[-1]
        if method in CROSSOVER_METHODS:
            try:
                base_words = int(entry["params"]["baseWords"])
                ratio = int(entry["params"]["operandRatio"])
                metric = entry["primaryMetric"]
                score = float(metric["score"])
                confidence = metric.get("scoreConfidence", [score, score])
                lower = float(confidence[0]) if len(confidence) > 0 else score
                upper = float(confidence[1]) if len(confidence) > 1 else score
            except (KeyError, TypeError, ValueError, IndexError) as error:
                raise ValueError(f"Malformed JMH crossover result for {method}: {error}") from error

            if (
                base_words not in EXPECTED_BASE_WORDS
                or ratio not in OPERAND_RATIOS
                or not all(math.isfinite(value) for value in (score, lower, upper))
                or score <= 0
            ):
                raise ValueError(f"Invalid crossover measurement for {method} at {base_words} words, ratio {ratio}:1")

            samples = crossover[ratio][method]
            if base_words in samples:
                raise ValueError(f"Duplicate crossover result for {method} at {base_words} words, ratio {ratio}:1")
            samples[base_words] = {"score": score, "lower": lower, "upper": upper}
            continue

        operation_key = next((key for key in OPERATIONS if method.startswith(key)), None)
        implementation_key = next((key for key in IMPLEMENTATIONS if method.endswith(key)), None)
        if operation_key is None or implementation_key is None:
            continue

        try:
            bits = int(entry["params"]["bits"])
            metric = entry["primaryMetric"]
            score = float(metric["score"])
            confidence = metric.get("scoreConfidence", [score, score])
            lower = float(confidence[0]) if len(confidence) > 0 else score
            upper = float(confidence[1]) if len(confidence) > 1 else score
        except (KeyError, TypeError, ValueError, IndexError) as error:
            raise ValueError(f"Malformed JMH result for {method}: {error}") from error

        if bits not in EXPECTED_BITS or not all(math.isfinite(value) for value in (score, lower, upper)) or score <= 0:
            raise ValueError(f"Invalid measurement for {method} at {bits} bits")

        operation = OPERATIONS[operation_key]
        samples = results[operation][implementation_key]
        if bits in samples:
            raise ValueError(f"Duplicate result for {operation} {implementation_key} at {bits} bits")
        samples[bits] = {"score": score, "lower": lower, "upper": upper}

    missing = []
    for operation, implementations in results.items():
        for implementation, samples in implementations.items():
            for bits in EXPECTED_BITS:
                if bits not in samples:
                    missing.append(f"{operation}/{implementation}/{bits}")
    if missing:
        raise ValueError("Incomplete benchmark run; missing: " + ", ".join(missing))

    missing_crossover = []
    for ratio, methods in crossover.items():
        for method, samples in methods.items():
            for base_words in EXPECTED_BASE_WORDS:
                if base_words not in samples:
                    missing_crossover.append(f"{method}/{base_words}words/{ratio}:1")
    if missing_crossover:
        raise ValueError("Incomplete multiplication crossover run; missing: " + ", ".join(missing_crossover))

    return results, crossover, jvm_version, jmh_version


def render_svg(
    results: CoreResults,
    generated_utc: str,
    jvm_version: str,
    jmh_version: str,
) -> str:
    width = 1200
    panel_width = 550
    panel_height = 235
    panel_x = [55, 625]
    panel_y = [120, 385, 650]
    height = 915
    parts = [
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}">',
        '<rect width="100%" height="100%" fill="#ffffff"/>',
        '<style>text{font-family:Arial,Helvetica,sans-serif;fill:#172033}.muted{fill:#5b6474}.grid{stroke:#d8dee8;stroke-width:1}.axis{stroke:#64748b;stroke-width:1}</style>',
        '<text x="55" y="42" font-size="26" font-weight="700">KBigInteger JVM latency by operand size</text>',
        f'<text x="55" y="68" font-size="13" class="muted">Average time (ns/op) · JMH {html.escape(jmh_version)} · JVM {html.escape(jvm_version)} · generated {html.escape(generated_utc)}</text>',
        '<line x1="790" y1="96" x2="820" y2="96" stroke="#2563eb" stroke-width="3"/><circle cx="805" cy="96" r="4" fill="#2563eb"/><text x="828" y="100" font-size="13">Java BigInteger</text>',
        '<line x1="970" y1="96" x2="1000" y2="96" stroke="#dc2626" stroke-width="3"/><circle cx="985" cy="96" r="4" fill="#dc2626"/><text x="1008" y="100" font-size="13">KBigInteger</text>',
    ]

    for operation_index, operation in enumerate(OPERATIONS.values()):
        x = panel_x[operation_index % 2]
        y = panel_y[operation_index // 2]
        plot_left = x + 62
        plot_right = x + panel_width - 14
        plot_top = y + 34
        plot_bottom = y + panel_height - 43
        plot_width = plot_right - plot_left
        plot_height = plot_bottom - plot_top
        maximum = max(
            results[operation][implementation][bits]["upper"]
            for implementation in IMPLEMENTATIONS
            for bits in EXPECTED_BITS
        )
        y_max = maximum * 1.12 if maximum > 0 else 1.0

        parts.append(f'<rect x="{x}" y="{y}" width="{panel_width}" height="{panel_height}" rx="8" fill="#fff" stroke="#d8dee8"/>')
        parts.append(f'<text x="{x + 14}" y="{y + 22}" font-size="16" font-weight="700">{html.escape(operation)}</text>')

        for tick in range(5):
            value = y_max * tick / 4
            tick_y = plot_bottom - (plot_height * tick / 4)
            parts.append(f'<line x1="{plot_left}" y1="{tick_y:.1f}" x2="{plot_right}" y2="{tick_y:.1f}" class="grid"/>')
            parts.append(f'<text x="{plot_left - 8}" y="{tick_y + 4:.1f}" font-size="10" text-anchor="end" class="muted">{value:.3g}</text>')

        parts.append(f'<line x1="{plot_left}" y1="{plot_top}" x2="{plot_left}" y2="{plot_bottom}" class="axis"/>')
        parts.append(f'<line x1="{plot_left}" y1="{plot_bottom}" x2="{plot_right}" y2="{plot_bottom}" class="axis"/>')

        def x_position(bits: int) -> float:
            return plot_left + (bits - EXPECTED_BITS[0]) / (EXPECTED_BITS[-1] - EXPECTED_BITS[0]) * plot_width

        def y_position(value: float) -> float:
            return plot_bottom - (value / y_max) * plot_height

        for bits in EXPECTED_BITS:
            bits_x = x_position(bits)
            parts.append(f'<line x1="{bits_x:.1f}" y1="{plot_bottom}" x2="{bits_x:.1f}" y2="{plot_bottom + 4}" class="axis"/>')
            parts.append(f'<text x="{bits_x:.1f}" y="{plot_bottom + 17}" font-size="10" text-anchor="middle" class="muted">{bits}</text>')

        for implementation in IMPLEMENTATIONS:
            color = COLORS[implementation]
            points = []
            for bits in EXPECTED_BITS:
                measurement = results[operation][implementation][bits]
                point_x = x_position(bits)
                point_y = y_position(measurement["score"])
                points.append(f"{point_x:.1f},{point_y:.1f}")

                low_y = y_position(max(0.0, measurement["lower"]))
                high_y = y_position(measurement["upper"])
                parts.append(f'<line x1="{point_x:.1f}" y1="{low_y:.1f}" x2="{point_x:.1f}" y2="{high_y:.1f}" stroke="{color}" stroke-width="1.5"/>')
                parts.append(f'<line x1="{point_x - 3:.1f}" y1="{low_y:.1f}" x2="{point_x + 3:.1f}" y2="{low_y:.1f}" stroke="{color}" stroke-width="1.5"/>')
                parts.append(f'<line x1="{point_x - 3:.1f}" y1="{high_y:.1f}" x2="{point_x + 3:.1f}" y2="{high_y:.1f}" stroke="{color}" stroke-width="1.5"/>')
                parts.append(f'<circle cx="{point_x:.1f}" cy="{point_y:.1f}" r="3.5" fill="{color}"/>')

            parts.append(f'<polyline points="{" ".join(points)}" fill="none" stroke="{color}" stroke-width="2.5" stroke-linejoin="round"/>')

        parts.append(f'<text x="{x + 15}" y="{y + panel_height - 10}" font-size="10" class="muted">Operand size (bits)</text>')
        parts.append(f'<text x="{x + panel_width - 9}" y="{y + panel_height - 10}" font-size="10" text-anchor="end" class="muted">lower is faster · JMH CI</text>')

    parts.append('<text x="55" y="903" font-size="11" class="muted">Fixed seeded operands; division and remainder use a 2:1 dividend-to-divisor bit-length ratio.</text>')
    parts.append('</svg>')
    return "\n".join(parts) + "\n"


def render_summary(
    results: CoreResults,
    crossover: CrossoverResults,
    generated_utc: str,
    jvm_version: str,
    jmh_version: str,
) -> str:
    header = "| Operation (KBigInteger / Java BigInteger) | " + " | ".join(f"{bits} bits" for bits in EXPECTED_BITS) + " |"
    separator = "| :--- | " + " | ".join(":---:" for _ in EXPECTED_BITS) + " |"
    rows = [header, separator]

    for operation in OPERATIONS.values():
        cells = []
        for bits in EXPECTED_BITS:
            java = results[operation]["Java"][bits]
            kbig = results[operation]["KBigNum"][bits]
            java_score = java["score"]
            kbig_score = kbig["score"]
            ratio = kbig_score / java_score
            ratio_text = f"{ratio:.2f}x"
            if kbig["upper"] < java["lower"]:
                cells.append(f"**↓ {ratio_text}**")
            elif kbig["lower"] > java["upper"]:
                cells.append(f"**↑ {ratio_text}**")
            else:
                cells.append(f"{ratio_text}†")
        rows.append(f"| {operation} | " + " | ".join(cells) + " |")

    crossover_rows = [
        "| Operand ratio (larger : smaller) | First size where production CI is below schoolbook CI |",
        "| :---: | :--- |",
    ]
    for ratio in OPERAND_RATIOS:
        clear_wins = [
            words
            for words in EXPECTED_BASE_WORDS
            if crossover[ratio]["multiplyProduction"][words]["upper"]
            < crossover[ratio]["multiplySchoolbookReference"][words]["lower"]
        ]
        result = f"{clear_wins[0]} words" if clear_wins else "No clear win in tested range"
        crossover_rows.append(f"| {ratio}:1 | {result} |")

    machine = f"{platform.system()} {platform.release()} / {platform.machine()}"
    return "\n".join(
        [
            "### KBigInteger latency ratio",
            "",
            "Each cell is `KBigInteger ns/op ÷ Java BigInteger ns/op`. `↓`/`↑` mark non-overlapping JMH intervals; `†` marks overlapping intervals, so the ratio is inconclusive.",
            "",
            "Interval overlap is a conservative visual cue, not a formal paired significance test.",
            "",
            *rows,
            "",
            "### Current multiplication crossover check",
            "",
            *crossover_rows,
            "",
            f"Generated {generated_utc} on {machine} with JMH {jmh_version} and JVM {jvm_version}. Data: [`latest.csv`](benchmarks/results/latest.csv) and [`multiplication-crossover.csv`](benchmarks/results/multiplication-crossover.csv).",
        ],
    )


def render_crossover_svg(
    results: CrossoverResults,
    generated_utc: str,
    jvm_version: str,
    jmh_version: str,
) -> str:
    width = 1200
    panel_x = 55
    panel_width = 1090
    panel_height = 225
    panel_y = [120, 370, 620]
    height = 880
    parts = [
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}">',
        '<rect width="100%" height="100%" fill="#ffffff"/>',
        '<style>text{font-family:Arial,Helvetica,sans-serif;fill:#172033}.muted{fill:#5b6474}.grid{stroke:#d8dee8;stroke-width:1}.axis{stroke:#64748b;stroke-width:1}</style>',
        '<text x="55" y="42" font-size="26" font-weight="700">Karatsuba crossover: production vs forced schoolbook</text>',
        f'<text x="55" y="68" font-size="13" class="muted">Average time (ns/op) · JMH {html.escape(jmh_version)} · JVM {html.escape(jvm_version)} · generated {html.escape(generated_utc)}</text>',
        '<line x1="710" y1="96" x2="740" y2="96" stroke="#dc2626" stroke-width="3"/><circle cx="725" cy="96" r="4" fill="#dc2626"/><text x="748" y="100" font-size="13">KBigInteger production path</text>',
        '<line x1="955" y1="96" x2="985" y2="96" stroke="#2563eb" stroke-width="3"/><circle cx="970" cy="96" r="4" fill="#2563eb"/><text x="993" y="100" font-size="13">Forced schoolbook reference</text>',
    ]

    for ratio in OPERAND_RATIOS:
        x = panel_x
        y = panel_y[OPERAND_RATIOS.index(ratio)]
        plot_left = x + 74
        plot_right = x + panel_width - 16
        plot_top = y + 34
        plot_bottom = y + panel_height - 40
        plot_width = plot_right - plot_left
        plot_height = plot_bottom - plot_top
        maximum = max(
            results[ratio][method][words]["upper"]
            for method in CROSSOVER_METHODS
            for words in EXPECTED_BASE_WORDS
        )
        y_max = maximum * 1.12 if maximum > 0 else 1.0

        parts.append(f'<rect x="{x}" y="{y}" width="{panel_width}" height="{panel_height}" rx="8" fill="#fff" stroke="#d8dee8"/>')
        parts.append(f'<text x="{x + 14}" y="{y + 22}" font-size="16" font-weight="700">Operand ratio {ratio}:1 (larger : smaller)</text>')

        for tick in range(5):
            value = y_max * tick / 4
            tick_y = plot_bottom - (plot_height * tick / 4)
            parts.append(f'<line x1="{plot_left}" y1="{tick_y:.1f}" x2="{plot_right}" y2="{tick_y:.1f}" class="grid"/>')
            parts.append(f'<text x="{plot_left - 8}" y="{tick_y + 4:.1f}" font-size="10" text-anchor="end" class="muted">{value:.3g}</text>')

        parts.append(f'<line x1="{plot_left}" y1="{plot_top}" x2="{plot_left}" y2="{plot_bottom}" class="axis"/>')
        parts.append(f'<line x1="{plot_left}" y1="{plot_bottom}" x2="{plot_right}" y2="{plot_bottom}" class="axis"/>')

        def x_position(words: int) -> float:
            return plot_left + (words - EXPECTED_BASE_WORDS[0]) / (EXPECTED_BASE_WORDS[-1] - EXPECTED_BASE_WORDS[0]) * plot_width

        def y_position(value: float) -> float:
            return plot_bottom - (value / y_max) * plot_height

        for words in EXPECTED_BASE_WORDS:
            point_x = x_position(words)
            parts.append(f'<line x1="{point_x:.1f}" y1="{plot_bottom}" x2="{point_x:.1f}" y2="{plot_bottom + 4}" class="axis"/>')
            parts.append(f'<text x="{point_x:.1f}" y="{plot_bottom + 16}" font-size="10" text-anchor="middle" class="muted">{words}</text>')

        for method in CROSSOVER_METHODS:
            color = STRATEGY_COLORS[method]
            points = []
            for words in EXPECTED_BASE_WORDS:
                measurement = results[ratio][method][words]
                point_x = x_position(words)
                point_y = y_position(measurement["score"])
                points.append(f"{point_x:.1f},{point_y:.1f}")

                low_y = y_position(max(0.0, measurement["lower"]))
                high_y = y_position(measurement["upper"])
                parts.append(f'<line x1="{point_x:.1f}" y1="{low_y:.1f}" x2="{point_x:.1f}" y2="{high_y:.1f}" stroke="{color}" stroke-width="1.5"/>')
                parts.append(f'<line x1="{point_x - 3:.1f}" y1="{low_y:.1f}" x2="{point_x + 3:.1f}" y2="{low_y:.1f}" stroke="{color}" stroke-width="1.5"/>')
                parts.append(f'<line x1="{point_x - 3:.1f}" y1="{high_y:.1f}" x2="{point_x + 3:.1f}" y2="{high_y:.1f}" stroke="{color}" stroke-width="1.5"/>')
                parts.append(f'<circle cx="{point_x:.1f}" cy="{point_y:.1f}" r="3.5" fill="{color}"/>')

            parts.append(f'<polyline points="{" ".join(points)}" fill="none" stroke="{color}" stroke-width="2.5" stroke-linejoin="round"/>')

        parts.append(f'<text x="{x + 15}" y="{y + panel_height - 8}" font-size="10" class="muted">Smaller operand size (32-bit words)</text>')
        parts.append(f'<text x="{x + panel_width - 9}" y="{y + panel_height - 8}" font-size="10" text-anchor="end" class="muted">lower is faster · JMH CI</text>')

    parts.append('<text x="55" y="865" font-size="11" class="muted">Production uses its current threshold; the reference forces the schoolbook limb loop. Outputs are equality-checked before timing.</text>')
    parts.append('</svg>')
    return "\n".join(parts) + "\n"


def write_csv(
    path: Path,
    results: CoreResults,
    jvm_version: str,
    jmh_version: str,
    generated_utc: str,
) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", newline="", encoding="utf-8") as output:
        writer = csv.writer(output, lineterminator="\n")
        writer.writerow(
            [
                "operation",
                "implementation",
                "bits",
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
                for bits in EXPECTED_BITS:
                    measurement = results[operation][implementation][bits]
                    writer.writerow(
                        [
                            operation,
                            IMPLEMENTATIONS[implementation],
                            bits,
                            f"{measurement['score']:.6f}",
                            f"{measurement['lower']:.6f}",
                            f"{measurement['upper']:.6f}",
                            jmh_version,
                            jvm_version,
                            machine,
                            generated_utc,
                        ],
                    )


def write_crossover_csv(
    path: Path,
    results: CrossoverResults,
    jvm_version: str,
    jmh_version: str,
    generated_utc: str,
) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", newline="", encoding="utf-8") as output:
        writer = csv.writer(output, lineterminator="\n")
        writer.writerow(
            [
                "strategy",
                "smaller_operand_words",
                "larger_operand_words",
                "operand_ratio",
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
        for ratio in OPERAND_RATIOS:
            for method, strategy in CROSSOVER_METHODS.items():
                for words in EXPECTED_BASE_WORDS:
                    measurement = results[ratio][method][words]
                    writer.writerow(
                        [
                            strategy,
                            words,
                            words * ratio,
                            f"{ratio}:1",
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
        raise ValueError("README must contain exactly one benchmark summary marker pair")
    start = readme.index(SUMMARY_START) + len(SUMMARY_START)
    end = readme.index(SUMMARY_END)
    if end < start:
        raise ValueError("README benchmark summary markers are out of order")
    path.write_text(readme[:start] + "\n\n" + summary + "\n\n" + readme[end:], encoding="utf-8")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("jmh_json", type=Path, help="JSON output produced by the JMH Gradle task")
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[1])
    args = parser.parse_args()

    results, crossover, jvm_version, jmh_version = parse_results(args.jmh_json)
    generated_utc = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M UTC")
    root = args.root.resolve()

    write_csv(root / "benchmarks/results/latest.csv", results, jvm_version, jmh_version, generated_utc)
    write_crossover_csv(root / "benchmarks/results/multiplication-crossover.csv", crossover, jvm_version, jmh_version, generated_utc)
    chart_path = root / "docs/assets/benchmark-integer-latency.svg"
    chart_path.parent.mkdir(parents=True, exist_ok=True)
    chart_path.write_text(render_svg(results, generated_utc, jvm_version, jmh_version), encoding="utf-8")
    crossover_chart_path = root / "docs/assets/benchmark-multiplication-crossover.svg"
    crossover_chart_path.write_text(render_crossover_svg(crossover, generated_utc, jvm_version, jmh_version), encoding="utf-8")
    update_readme(root / "README.md", render_summary(results, crossover, generated_utc, jvm_version, jmh_version))
    print(
        f"Updated {chart_path.relative_to(root)}, {crossover_chart_path.relative_to(root)}, and README benchmark summary",
        flush=True,
    )
    decimal_renderer = root / "scripts/render_decimal_benchmarks.py"
    subprocess.run(
        [sys.executable, str(decimal_renderer), str(args.jmh_json), "--root", str(root)],
        check=True,
    )


if __name__ == "__main__":
    main()
