import argparse
import csv
import random
from pathlib import Path


GROUP_COLUMNS = [
    ("food_drink_count", 1),
    ("transport_count", 2),
    ("shopping_count", 3),
    ("entertainment_count", 5),
    ("health_count", 6),
]


def parse_number(value: str) -> float:
    if not value:
        return 0.0
    return float(value.replace(",", "."))


def expected_category(row: dict[str, str]) -> int | None:
    values = [(parse_number(row[column]), category_id) for column, category_id in GROUP_COLUMNS]
    maximum = max(value for value, _ in values)
    winners = [category_id for value, category_id in values if value == maximum]
    if maximum <= 0 or len(winners) != 1:
        return None
    return winners[0]


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    parser.add_argument("--changes", default=20, type=int)
    parser.add_argument("--seed", default=5, type=int)
    args = parser.parse_args()

    with args.input.open("r", encoding="utf-8-sig", newline="") as file:
        reader = csv.DictReader(file, delimiter=";")
        fieldnames = reader.fieldnames
        rows = list(reader)

    candidates = [
        index
        for index, row in enumerate(rows)
        if expected_category(row) is not None and int(row["category_id"]) != expected_category(row)
    ]
    selected = random.Random(args.seed).sample(candidates, args.changes)
    for index in selected:
        rows[index]["category_id"] = str(expected_category(rows[index]))

    args.output.parent.mkdir(parents=True, exist_ok=True)
    with args.output.open("w", encoding="utf-8-sig", newline="") as file:
        writer = csv.DictWriter(file, fieldnames=fieldnames, delimiter=";", lineterminator="\n")
        writer.writeheader()
        writer.writerows(rows)

    print(f"rows={len(rows)}, candidates={len(candidates)}, changed={len(selected)}")
    print(f"changed_row_numbers={','.join(str(index + 2) for index in selected)}")


if __name__ == "__main__":
    main()
