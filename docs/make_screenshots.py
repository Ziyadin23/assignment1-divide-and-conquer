"""Make readable screenshots from the saved run logs and results.

Run from the project folder with: python3 docs/make_screenshots.py
The two log files are created when the program and Maven tests are run.
"""

import csv
from collections import defaultdict
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parent.parent
OUTPUT = ROOT / "docs" / "screenshots"
PLOTS = ROOT / "docs" / "plots"
PROGRAM_LOG = ROOT / "docs" / "logs" / "program_output.txt"
TEST_LOG = ROOT / "docs" / "logs" / "maven_output.txt"

MONO_FONT = "/usr/share/fonts/adobe-source-code-pro-fonts/SourceCodePro-Medium.otf"
SANS_FONT = "/usr/share/fonts/open-sans/OpenSans-Regular.ttf"
BOLD_FONT = "/usr/share/fonts/open-sans/OpenSans-Bold.ttf"


def font(path, size):
    if Path(path).exists():
        return ImageFont.truetype(path, size)
    return ImageFont.load_default()


def terminal_screenshot(title, lines, filename):
    code_font = font(MONO_FONT, 23)
    title_font = font(BOLD_FONT, 27)
    line_height = 30
    left = 60
    top = 128
    widest = max((code_font.getlength(line) for line in lines), default=0)
    width = max(1100, int(widest) + 120)
    height = top + len(lines) * line_height + 55

    image = Image.new("RGB", (width, height), "#e8edf3")
    draw = ImageDraw.Draw(image)
    draw.rounded_rectangle((20, 20, width - 20, height - 20), radius=20,
                           fill="#101820")
    draw.rounded_rectangle((20, 20, width - 20, 92), radius=20,
                           fill="#263544")
    draw.rectangle((20, 72, width - 20, 92), fill="#263544")
    for x, color in [(55, "#ff6b6b"), (85, "#f7c65e"), (115, "#59c985")]:
        draw.ellipse((x - 8, 48, x + 8, 64), fill=color)
    draw.text((150, 41), title, font=title_font, fill="#f3f6fa")

    for number, line in enumerate(lines):
        color = "#dbe6f1"
        if "BUILD SUCCESS" in line or "Failures: 0" in line:
            color = "#87dda3"
        elif line.startswith("[WARNING]"):
            color = "#ffd479"
        elif line.startswith("Divide-and-Conquer") or line.startswith("Algorithm "):
            color = "#9ed4ff"
        draw.text((left, top + number * line_height), line,
                  font=code_font, fill=color)

    image.save(OUTPUT / filename)


def average_rows():
    samples = defaultdict(list)
    with (ROOT / "results" / "results.csv").open(newline="") as handle:
        for row in csv.DictReader(handle):
            if row["input_type"] == "random":
                key = (row["algorithm"], int(row["n"]))
                samples[key].append(row)

    rows = []
    order = ["MergeSort", "QuickSort", "DeterministicSelect", "ClosestPair"]
    for algorithm in order:
        for size in [200, 2000, 20000]:
            values = samples[(algorithm, size)]
            if not values:
                continue
            time_ms = sum(int(value["time_ns"]) for value in values) / len(values) / 1e6
            depth = sum(int(value["max_depth"]) for value in values) / len(values)
            rows.append((algorithm, f"{size:,}", f"{time_ms:.3f}",
                         f"{depth:.1f}", str(len(values))))
    return rows


def paste_plot(canvas, plot_path, box):
    plot = Image.open(plot_path).convert("RGB")
    plot.thumbnail((box[2] - box[0] - 26, box[3] - box[1] - 26),
                   Image.Resampling.LANCZOS)
    x = box[0] + (box[2] - box[0] - plot.width) // 2
    y = box[1] + (box[3] - box[1] - plot.height) // 2
    canvas.paste(plot, (x, y))


def plots_and_results_screenshot():
    time_plot = PLOTS / "time_vs_n.png"
    depth_plot = PLOTS / "depth_vs_n.png"
    for path in [time_plot, depth_plot]:
        if not path.exists():
            raise FileNotFoundError(f"Make the plot first: {path}")

    width, height = 2200, 1620
    image = Image.new("RGB", (width, height), "#e8edf3")
    draw = ImageDraw.Draw(image)
    title_font = font(BOLD_FONT, 42)
    subtitle_font = font(SANS_FONT, 22)
    header_font = font(BOLD_FONT, 23)
    cell_font = font(MONO_FONT, 21)

    draw.text((64, 48), "Experimental results", font=title_font, fill="#172538")
    draw.text((65, 110), "Plots and averages from results/results.csv (random input, 5 trials)",
              font=subtitle_font, fill="#405266")

    left_box = (50, 170, 1080, 870)
    right_box = (1120, 170, 2150, 870)
    for box in [left_box, right_box]:
        draw.rounded_rectangle(box, radius=18, fill="white")
    paste_plot(image, time_plot, left_box)
    paste_plot(image, depth_plot, right_box)

    draw.rounded_rectangle((50, 915, 2150, 1570), radius=18, fill="white")
    draw.text((90, 950), "Average results for random input", font=header_font,
              fill="#172538")
    headers = ["Algorithm", "n", "Time (ms)", "Max depth", "Trials"]
    columns = [90, 650, 920, 1260, 1620]
    for x, name in zip(columns, headers):
        draw.text((x, 1005), name, font=header_font, fill="#334d68")
    draw.line((90, 1045, 2080, 1045), fill="#cfd8e3", width=2)

    for index, row in enumerate(average_rows()):
        y = 1065 + index * 39
        if index % 2 == 0:
            draw.rounded_rectangle((78, y - 3, 2080, y + 34), radius=6,
                                   fill="#f3f6fa")
        for x, value in zip(columns, row):
            draw.text((x, y), value, font=cell_font, fill="#24364a")

    image.save(OUTPUT / "plots_and_results.png")


def main():
    OUTPUT.mkdir(parents=True, exist_ok=True)
    program_lines = PROGRAM_LOG.read_text().splitlines()
    terminal_screenshot("Program output", program_lines, "program_output.png")

    test_lines = TEST_LOG.read_text().splitlines()
    start = next(i for i, line in enumerate(test_lines)
                 if "--- surefire:" in line)
    terminal_screenshot("Maven test results", test_lines[start:], "test_results.png")
    plots_and_results_screenshot()


if __name__ == "__main__":
    main()
