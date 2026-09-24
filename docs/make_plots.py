"""Make the two report charts from the benchmark CSV file."""

import csv
import math
from collections import defaultdict
from pathlib import Path
from statistics import mean

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
CSV_FILE = ROOT / "results" / "results.csv"
PLOT_DIR = ROOT / "docs" / "plots"

ALGORITHMS = ["MergeSort", "QuickSort", "DeterministicSelect", "ClosestPair"]
COLORS = {
    "MergeSort": "#0072B2",
    "QuickSort": "#D55E00",
    "DeterministicSelect": "#009E73",
    "ClosestPair": "#CC79A7",
}


def font(size, bold=False):
    name = "NotoSans-Bold.ttf" if bold else "NotoSans-Regular.ttf"
    path = Path("/usr/share/fonts/google-noto") / name
    if path.exists():
        return ImageFont.truetype(str(path), size)
    return ImageFont.load_default()


def read_averages():
    groups = defaultdict(list)
    with CSV_FILE.open(newline="", encoding="utf-8") as file:
        for row in csv.DictReader(file):
            if row["input_type"] == "random" and row["algorithm"] in ALGORITHMS:
                groups[(row["algorithm"], int(row["n"]))].append(row)

    sizes = sorted({size for _, size in groups})
    if not sizes:
        raise ValueError("No random input results found in the CSV file")

    averages = {}
    for algorithm in ALGORITHMS:
        for size in sizes:
            rows = groups[(algorithm, size)]
            if not rows:
                raise ValueError(f"Missing results for {algorithm}, n={size}")
            averages[(algorithm, size)] = {
                "time_ms": mean(int(row["time_ns"]) for row in rows) / 1_000_000,
                "depth": mean(int(row["max_depth"]) for row in rows),
            }
    return sizes, averages


def tick_step(maximum):
    rough_step = maximum / 5
    power = 10 ** math.floor(math.log10(rough_step))
    for factor in (1, 2, 5, 10):
        step = factor * power
        if step >= rough_step:
            return step
    return 10 * power


def make_plot(sizes, averages, key, title, y_label, output_file):
    width, height = 1250, 760
    left, right, top, bottom = 150, 1150, 175, 635
    image = Image.new("RGB", (width, height), "white")
    draw = ImageDraw.Draw(image)
    dark = "#1F2937"
    muted = "#667085"
    grid = "#E5E7EB"

    draw.text((left, 27), title, fill=dark, font=font(31, bold=True))
    draw.text((left, 78), "Random input; average of 5 trials at each size",
              fill=muted, font=font(18))

    for index, algorithm in enumerate(ALGORITHMS):
        x = left + index * 255
        color = COLORS[algorithm]
        draw.line((x, 132, x + 33, 132), fill=color, width=5)
        draw.ellipse((x + 12, 126, x + 24, 138), fill=color)
        draw.text((x + 43, 119), algorithm, fill=dark, font=font(17))

    maximum = max(averages[(algorithm, size)][key]
                  for algorithm in ALGORITHMS for size in sizes)
    step = tick_step(maximum * 1.1)
    y_max = math.ceil(maximum * 1.1 / step) * step

    def x_position(size):
        first = math.log10(sizes[0])
        last = math.log10(sizes[-1])
        if first == last:
            return (left + right) / 2
        return left + (math.log10(size) - first) / (last - first) * (right - left)

    def y_position(value):
        return bottom - value / y_max * (bottom - top)

    tick = 0
    while tick <= y_max + step / 100:
        y = round(y_position(tick))
        draw.line((left, y, right, y), fill=grid, width=2)
        label = f"{tick:g}"
        box = draw.textbbox((0, 0), label, font=font(16))
        draw.text((left - 18 - (box[2] - box[0]), y - 11), label,
                  fill=muted, font=font(16))
        tick += step

    draw.line((left, top, left, bottom), fill=dark, width=2)
    draw.line((left, bottom, right, bottom), fill=dark, width=2)

    for size in sizes:
        x = round(x_position(size))
        draw.line((x, bottom, x, bottom + 8), fill=dark, width=2)
        label = f"{size:,}"
        box = draw.textbbox((0, 0), label, font=font(18))
        draw.text((x - (box[2] - box[0]) / 2, bottom + 14), label,
                  fill=dark, font=font(18))

    for algorithm in ALGORITHMS:
        points = [(round(x_position(size)), round(y_position(averages[(algorithm, size)][key])))
                  for size in sizes]
        draw.line(points, fill=COLORS[algorithm], width=4, joint="curve")
        for x, y in points:
            draw.ellipse((x - 6, y - 6, x + 6, y + 6),
                         fill=COLORS[algorithm], outline="white", width=2)

    x_label = "Input size n (log scale)"
    box = draw.textbbox((0, 0), x_label, font=font(18))
    draw.text(((left + right - (box[2] - box[0])) / 2, 693), x_label,
              fill=dark, font=font(18))

    y_text = Image.new("RGBA", (400, 35), (255, 255, 255, 0))
    ImageDraw.Draw(y_text).text((0, 0), y_label, fill=dark, font=font(18))
    rotated = y_text.rotate(90, expand=True)
    image.paste(rotated, (30, (top + bottom - rotated.height) // 2), rotated)

    draw.text((left, 731), "Source: results/results.csv", fill=muted, font=font(14))
    image.save(output_file)


def main():
    sizes, averages = read_averages()
    PLOT_DIR.mkdir(parents=True, exist_ok=True)
    make_plot(sizes, averages, "time_ms", "Running time vs input size",
              "Mean time (ms)", PLOT_DIR / "time_vs_n.png")
    make_plot(sizes, averages, "depth", "Recursion depth vs input size",
              "Mean maximum depth", PLOT_DIR / "depth_vs_n.png")


if __name__ == "__main__":
    main()
