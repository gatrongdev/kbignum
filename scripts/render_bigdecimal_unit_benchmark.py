#!/usr/bin/env python3
"""Render the checked-in KBigDecimal scaled-unit multiplication A/B data as SVG."""

from __future__ import annotations

import argparse
import csv
import html
import math
from pathlib import Path


INPUT_DIGITS = {
    "JVM": (500, 1000),
    "Kotlin/JS Node.js": (500, 1000, 2000),
}
COLORS = {"baseline": "#94a3b8", "candidate": "#2563eb"}


def read_rows(path: Path) -> list[dict[str, str]]:
    with path.open(encoding="utf-8", newline="") as source:
        rows = list(csv.DictReader(source))
    unit_rows = [row for row in rows if row["workload"] == "scaled-unit"]
    for runtime, expected_digits in INPUT_DIGITS.items():
        found = sorted(int(row["input_digits"]) for row in unit_rows if row["runtime"] == runtime)
        if tuple(found) != expected_digits:
            raise ValueError(f"Expected {runtime} scaled-unit measurements at {expected_digits}, got {found}")
    for row in unit_rows:
        for field in ("baseline_ns_op", "candidate_ns_op"):
            value = float(row[field])
            if not math.isfinite(value) or value <= 0:
                raise ValueError(f"Invalid {field} for {row['runtime']} / {row['input_digits']}")
    return unit_rows


def render(rows: list[dict[str, str]]) -> str:
    width, height = 1100, 600
    panels = (("JVM", 75, 1000), ("Kotlin/JS Node.js", 585, 100000))
    plot_top, plot_bottom = 185, 475
    plot_height = plot_bottom - plot_top
    svg = [
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}" role="img" aria-labelledby="title description">',
        '<title id="title">Scaled-unit KBigDecimal multiplication latency</title>',
        '<desc id="description">Logarithmic latency comparison between the generic multiplication path and the plus-or-minus-one fast path for large scaled decimals on JVM and Node.js.</desc>',
        '<rect width="100%" height="100%" fill="#fff"/>',
        '<style>text{font-family:Arial,Helvetica,sans-serif;fill:#172033}.muted{fill:#64748b}.grid{stroke:#e2e8f0;stroke-width:1}.axis{stroke:#64748b;stroke-width:1}.title{font-size:23px;font-weight:700}.panel{font-size:16px;font-weight:700}.small{font-size:11px}</style>',
        '<text x="55" y="42" class="title">Large KBigDecimal × scaled unit</text>',
        '<text x="55" y="68" class="muted" font-size="13">Latency per operation · lower is better · logarithmic scale</text>',
        f'<rect x="55" y="91" width="14" height="14" rx="2" fill="{COLORS["baseline"]}"/><text x="76" y="103" font-size="12">Generic multiplication path</text>',
        f'<rect x="270" y="91" width="14" height="14" rx="2" fill="{COLORS["candidate"]}"/><text x="291" y="103" font-size="12">±1 fast path</text>',
        '<line x1="430" y1="98" x2="458" y2="98" stroke="#172033" stroke-width="1.5"/><line x1="430" y1="94" x2="430" y2="102" stroke="#172033"/><line x1="458" y1="94" x2="458" y2="102" stroke="#172033"/><text x="466" y="103" font-size="12">JMH 99.9% confidence interval</text>',
    ]

    for runtime, left, maximum in panels:
        panel_rows = sorted((row for row in rows if row["runtime"] == runtime), key=lambda row: int(row["input_digits"]))
        plot_left, plot_right = left + 62, left + 435
        minimum = 1.0
        for tick in (1, 10, 100, 1000, 10000, 100000):
            if tick > maximum:
                continue
            y = plot_bottom - (math.log10(tick) / math.log10(maximum)) * plot_height
            svg.append(f'<line x1="{plot_left}" y1="{y:.1f}" x2="{plot_right}" y2="{y:.1f}" class="grid"/>')
            svg.append(f'<text x="{plot_left - 8}" y="{y + 4:.1f}" text-anchor="end" class="small">{tick:g}</text>')

        svg.extend((
            f'<text x="{left}" y="145" class="panel">{html.escape(runtime)} · ns/op</text>',
            f'<line x1="{plot_left}" y1="{plot_top}" x2="{plot_left}" y2="{plot_bottom}" class="axis"/>',
            f'<line x1="{plot_left}" y1="{plot_bottom}" x2="{plot_right}" y2="{plot_bottom}" class="axis"/>',
        ))

        step = (plot_right - plot_left) / len(panel_rows)
        for index, row in enumerate(panel_rows):
            center = plot_left + step * (index + 0.5)
            digit = int(row["input_digits"])
            svg.append(f'<text x="{center:.1f}" y="{plot_bottom + 22}" text-anchor="middle" class="small">{digit} digits</text>')
            for offset, value_field, low_field, high_field, color in (
                (-13, "baseline_ns_op", "baseline_ci_low_ns_op", "baseline_ci_high_ns_op", COLORS["baseline"]),
                (13, "candidate_ns_op", "candidate_ci_low_ns_op", "candidate_ci_high_ns_op", COLORS["candidate"]),
            ):
                value = float(row[value_field])
                y = plot_bottom - (math.log10(value) / math.log10(maximum)) * plot_height
                bar_x = center + offset - 8
                bar_height = max(1.0, plot_bottom - y)
                svg.append(f'<rect x="{bar_x:.1f}" y="{y:.1f}" width="16" height="{bar_height:.1f}" rx="2" fill="{color}"/>')
                svg.append(f'<text x="{center + offset:.1f}" y="{y - 6:.1f}" text-anchor="middle" class="small">{value:,.0f}</text>')
                low_text, high_text = row[low_field], row[high_field]
                if low_text and high_text:
                    low = max(minimum, float(low_text))
                    high = min(maximum, float(high_text))
                    low_y = plot_bottom - (math.log10(low) / math.log10(maximum)) * plot_height
                    high_y = plot_bottom - (math.log10(high) / math.log10(maximum)) * plot_height
                    x = center + offset
                    svg.extend((
                        f'<line x1="{x:.1f}" y1="{high_y:.1f}" x2="{x:.1f}" y2="{low_y:.1f}" stroke="#172033" stroke-width="1.2"/>',
                        f'<line x1="{x - 4:.1f}" y1="{high_y:.1f}" x2="{x + 4:.1f}" y2="{high_y:.1f}" stroke="#172033" stroke-width="1.2"/>',
                        f'<line x1="{x - 4:.1f}" y1="{low_y:.1f}" x2="{x + 4:.1f}" y2="{low_y:.1f}" stroke="#172033" stroke-width="1.2"/>',
                    ))

            speedup = float(row["baseline_ns_op"]) / float(row["candidate_ns_op"])
            svg.append(f'<text x="{center:.1f}" y="{plot_bottom + 48}" text-anchor="middle" class="small muted">{speedup:.1f}× faster</text>')

    svg.extend((
        '<text x="55" y="555" class="muted" font-size="11">JVM bars show JMH means and 99.9% intervals. Node.js uses 2000 warmups and medians of 15 samples × 100 operations.</text>',
        '<text x="55" y="575" class="muted" font-size="11">Input digits describe the unscaled coefficient; the right decimal has unscaled value 1 and scale 16.</text>',
        '</svg>',
    ))
    return "\n".join(svg) + "\n"


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("csv", type=Path, help="CSV created from paired JVM and Node.js measurements")
    parser.add_argument("--output", type=Path, default=Path("docs/assets/benchmark-bigdecimal-multiply-unit.svg"))
    args = parser.parse_args()
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(render(read_rows(args.csv)), encoding="utf-8")
    print(f"Updated {args.output}")


if __name__ == "__main__":
    main()
