import argparse
import csv
import os
import re
import shutil
import tempfile
import xml.etree.ElementTree as ET
import zipfile
from pathlib import Path


FEATURE_NAMES = [
    "has_bottled_water",
    "has_bread",
    "has_clothes",
    "has_coffee_cup",
    "has_cosmetic",
    "has_electronic_item",
    "has_fastfood",
    "has_helmet",
    "has_medicine",
    "has_milk_tea",
    "has_motorbike",
    "has_noodle",
    "has_rice_meal",
    "has_shoes",
    "has_snack",
    "has_soft_drink",
    "has_taxi_car",
    "has_toy_game",
    "food_drink_count",
    "transport_count",
    "shopping_count",
    "entertainment_count",
    "health_count",
    "total_objects",
    "max_confidence",
    "avg_confidence",
    "low_confidence_count",
]

CATEGORY_NAMES = {
    1: "An uong",
    2: "Di chuyen",
    3: "Mua sam",
    5: "Giai tri",
    6: "Suc khoe",
    7: "Khac",
}

MAIN_NS = "http://schemas.openxmlformats.org/spreadsheetml/2006/main"
MC_NS = "http://schemas.openxmlformats.org/markup-compatibility/2006"
NS = {"m": MAIN_NS}
ET.register_namespace("", MAIN_NS)

FEATURE_COLUMNS = [
    "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
    "S", "T", "U", "V", "W", "X", "Y", "Z", "AA", "AB", "AC", "AD", "AE",
]


def parse_number(value: str) -> float:
    if not value:
        return 0.0
    return float(value.replace(",", "."))


def load_csv(path: Path) -> tuple[dict[tuple[float, ...], int], dict[tuple[float, ...], list[str]]]:
    labels = {}
    values = {}
    with path.open("r", encoding="utf-8-sig", newline="") as file:
        for row in csv.DictReader(file, delimiter=";"):
            key = tuple(parse_number(row[name]) for name in FEATURE_NAMES)
            labels[key] = int(row["category_id"])
            values[key] = [row[name].replace(",", ".") for name in FEATURE_NAMES]
    return labels, values


def column_name(cell: ET.Element) -> str:
    return re.match(r"[A-Z]+", cell.attrib["r"]).group()


def row_feature_key(row: ET.Element) -> tuple[float, ...]:
    cells = {column_name(cell): cell for cell in row.findall("m:c", NS)}
    result = []
    for column in FEATURE_COLUMNS:
        cell = cells.get(column)
        value = cell.find("m:v", NS) if cell is not None else None
        result.append(float(value.text) if value is not None else 0.0)
    return tuple(result)


def set_number_cell(row: ET.Element, column: str, row_number: int, value: str) -> None:
    cells = {column_name(cell): cell for cell in row.findall("m:c", NS)}
    cell = cells.get(column)
    if cell is None:
        cell = ET.SubElement(row, f"{{{MAIN_NS}}}c", {"r": f"{column}{row_number}"})
    cell.attrib.pop("t", None)
    for child in list(cell):
        cell.remove(child)
    ET.SubElement(cell, f"{{{MAIN_NS}}}v").text = value


def set_text_cell(row: ET.Element, column: str, row_number: int, value: str) -> None:
    cells = {column_name(cell): cell for cell in row.findall("m:c", NS)}
    cell = cells.get(column)
    if cell is None:
        cell = ET.SubElement(row, f"{{{MAIN_NS}}}c", {"r": f"{column}{row_number}"})
    cell.attrib["t"] = "inlineStr"
    for child in list(cell):
        cell.remove(child)
    inline = ET.SubElement(cell, f"{{{MAIN_NS}}}is")
    ET.SubElement(inline, f"{{{MAIN_NS}}}t").text = value


def prepare_sheet(
    workbook: Path,
    csv_labels: dict[tuple[float, ...], int],
    used_keys: set[tuple[float, ...]],
) -> tuple[ET.ElementTree, int, int, int]:
    with zipfile.ZipFile(workbook) as archive:
        tree = ET.ElementTree(ET.fromstring(archive.read("xl/worksheets/sheet1.xml")))

    root = tree.getroot()
    root.attrib.pop(f"{{{MC_NS}}}Ignorable", None)
    sheet_data = root.find("m:sheetData", NS)
    rows = list(sheet_data.findall("m:row", NS))
    kept = 0
    removed = 0
    changed = 0

    for row in rows[1:]:
        key = row_feature_key(row)
        if key not in csv_labels or key in used_keys:
            sheet_data.remove(row)
            removed += 1
            continue

        used_keys.add(key)
        kept += 1
        row_number = int(row.attrib["r"])
        cells = {column_name(cell): cell for cell in row.findall("m:c", NS)}
        old_category_cell = cells.get("AF")
        old_value = old_category_cell.find("m:v", NS) if old_category_cell is not None else None
        old_category = int(float(old_value.text)) if old_value is not None else None
        category_id = csv_labels[key]
        if old_category != category_id:
            changed += 1
        set_number_cell(row, "AF", row_number, str(category_id))
        set_text_cell(row, "AG", row_number, CATEGORY_NAMES[category_id])

    return tree, kept, removed, changed


def append_missing_rows(
    tree: ET.ElementTree,
    missing_keys: list[tuple[float, ...]],
    csv_labels: dict[tuple[float, ...], int],
    csv_values: dict[tuple[float, ...], list[str]],
) -> None:
    root = tree.getroot()
    sheet_data = root.find("m:sheetData", NS)
    existing_rows = sheet_data.findall("m:row", NS)
    next_row = max(int(row.attrib["r"]) for row in existing_rows) + 1

    for key in missing_keys:
        row = ET.SubElement(sheet_data, f"{{{MAIN_NS}}}row", {"r": str(next_row)})
        for column, value in zip(FEATURE_COLUMNS, csv_values[key]):
            set_number_cell(row, column, next_row, value)
        category_id = csv_labels[key]
        set_number_cell(row, "AF", next_row, str(category_id))
        set_text_cell(row, "AG", next_row, CATEGORY_NAMES[category_id])
        next_row += 1

    dimension = root.find("m:dimension", NS)
    if dimension is not None:
        dimension.attrib["ref"] = f"A1:AG{next_row - 1}"


def write_workbook(workbook: Path, tree: ET.ElementTree) -> None:
    sheet_xml = ET.tostring(tree.getroot(), encoding="utf-8", xml_declaration=True)
    file_descriptor, temp_name = tempfile.mkstemp(suffix=".xlsx", dir=workbook.parent)
    os.close(file_descriptor)
    temp_file = Path(temp_name)
    try:
        with zipfile.ZipFile(workbook, "r") as source, zipfile.ZipFile(
            temp_file, "w", allowZip64=True
        ) as destination:
            for item in source.infolist():
                if item.filename == "xl/worksheets/sheet1.xml":
                    destination.writestr(item, sheet_xml)
                else:
                    with source.open(item) as input_file, destination.open(item, "w") as output_file:
                        shutil.copyfileobj(input_file, output_file, length=1024 * 1024)
        temp_file.replace(workbook)
    finally:
        temp_file.unlink(missing_ok=True)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--csv", required=True, type=Path)
    parser.add_argument("--primary", required=True, type=Path)
    parser.add_argument("--secondary", required=True, type=Path)
    args = parser.parse_args()

    csv_labels, csv_values = load_csv(args.csv)
    used_keys: set[tuple[float, ...]] = set()

    primary_tree, primary_kept, primary_removed, primary_changed = prepare_sheet(
        args.primary, csv_labels, used_keys
    )
    secondary_tree, secondary_kept, secondary_removed, secondary_changed = prepare_sheet(
        args.secondary, csv_labels, used_keys
    )

    missing_keys = [key for key in csv_labels if key not in used_keys]
    append_missing_rows(primary_tree, missing_keys, csv_labels, csv_values)

    write_workbook(args.primary, primary_tree)
    write_workbook(args.secondary, secondary_tree)

    print(
        f"primary: kept={primary_kept}, added={len(missing_keys)}, "
        f"removed={primary_removed}, changed={primary_changed}"
    )
    print(
        f"secondary: kept={secondary_kept}, removed={secondary_removed}, "
        f"changed={secondary_changed}"
    )
    print(f"combined_rows={primary_kept + secondary_kept + len(missing_keys)}")


if __name__ == "__main__":
    main()
