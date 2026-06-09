import argparse
import csv
from collections import Counter
from pathlib import Path

try:
    import joblib
    from sklearn.ensemble import RandomForestClassifier
    from sklearn.metrics import accuracy_score, classification_report, confusion_matrix
    from sklearn.model_selection import train_test_split
except ModuleNotFoundError as error:
    raise SystemExit(
        f"Missing Python package '{error.name}'. Install training dependencies with:\n"
        "python -m pip install -r tools/requirements-product-random-forest.txt"
    ) from error


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
    1: "food",
    2: "transport",
    3: "shopping",
    5: "entertainment",
    6: "health",
    7: "other",
}

REPO_ROOT = Path(__file__).resolve().parent.parent
DEFAULT_INPUT = REPO_ROOT / "tools" / "data.csv"
DEFAULT_OUTPUT = REPO_ROOT / "tools" / "product_random_forest.joblib"


def parse_number(value: str | None) -> float:
    if value is None or not str(value).strip():
        return 0.0
    return float(str(value).strip().replace(",", "."))


def load_dataset(csv_path: Path) -> tuple[list[list[float]], list[int]]:
    features: list[list[float]] = []
    labels: list[int] = []

    with csv_path.open("r", encoding="utf-8-sig", newline="") as file:
        reader = csv.DictReader(file, delimiter=";")
        fieldnames = reader.fieldnames or []
        missing = [column for column in [*FEATURE_NAMES, "category_id"] if column not in fieldnames]
        if missing:
            raise ValueError(f"CSV missing columns: {', '.join(missing)}")

        for line_number, row in enumerate(reader, start=2):
            try:
                sample = [parse_number(row[name]) for name in FEATURE_NAMES]
                label = int(parse_number(row["category_id"]))
            except (TypeError, ValueError) as error:
                raise ValueError(f"Invalid numeric value at {csv_path}:{line_number}: {error}") from error

            if label not in CATEGORY_NAMES:
                raise ValueError(f"Unknown category_id {label} at {csv_path}:{line_number}")

            features.append(sample)
            labels.append(label)

    return features, labels


def remove_bad_rows(
    features: list[list[float]], labels: list[int]
) -> tuple[list[list[float]], list[int], int, int, int]:
    labels_by_features: dict[tuple[float, ...], set[int]] = {}
    for sample, label in zip(features, labels):
        labels_by_features.setdefault(tuple(sample), set()).add(label)

    conflicts = {sample for sample, sample_labels in labels_by_features.items() if len(sample_labels) > 1}
    consistent = [
        (tuple(sample), label)
        for sample, label in zip(features, labels)
        if tuple(sample) not in conflicts
    ]
    unique = list(dict.fromkeys(consistent))

    conflict_rows = len(features) - len(consistent)
    duplicate_rows = len(consistent) - len(unique)
    clean_features = [list(sample) for sample, _ in unique]
    clean_labels = [label for _, label in unique]
    return clean_features, clean_labels, duplicate_rows, len(conflicts), conflict_rows


def train_random_forest(
    csv_path: Path,
    output_path: Path,
    trees: int,
    max_depth: int,
    test_size: float,
    seed: int,
) -> None:
    if not 0.0 < test_size < 1.0:
        raise ValueError("--test-size must be between 0 and 1.")
    if trees < 1:
        raise ValueError("--trees must be at least 1.")
    if max_depth < 1:
        raise ValueError("--max-depth must be at least 1.")

    features, labels = load_dataset(csv_path)
    features, labels, duplicate_rows, conflict_vectors, conflict_rows = remove_bad_rows(features, labels)
    if len(features) < 2:
        raise ValueError("Dataset needs at least 2 valid samples.")

    label_counts = Counter(labels)
    print(f"input={csv_path}")
    print(
        f"samples={len(features)}, removed_duplicates={duplicate_rows}, "
        f"removed_conflicts={conflict_vectors} vectors/{conflict_rows} rows, "
        f"labels={dict(sorted(label_counts.items()))}"
    )

    stratify = labels if min(label_counts.values()) >= 2 else None
    train_x, test_x, train_y, test_y = train_test_split(
        features,
        labels,
        test_size=test_size,
        random_state=seed,
        stratify=stratify,
    )

    model = RandomForestClassifier(
        n_estimators=trees,
        max_depth=max_depth,
        random_state=seed,
        class_weight="balanced",
        n_jobs=-1,
    )
    model.fit(train_x, train_y)

    predictions = model.predict(test_x)
    print(f"accuracy={accuracy_score(test_y, predictions):.4f}")
    print(classification_report(test_y, predictions, zero_division=0))
    print("confusion_matrix=")
    print(confusion_matrix(test_y, predictions))

    model.fit(features, labels)
    payload = {
        "model": model,
        "feature_names": FEATURE_NAMES,
        "category_names": CATEGORY_NAMES,
    }
    output_path.parent.mkdir(parents=True, exist_ok=True)
    joblib.dump(payload, output_path)
    print(f"saved={output_path}")


def main() -> None:
    parser = argparse.ArgumentParser(description="Train product RandomForest and save a joblib model.")
    parser.add_argument("--input", type=Path, default=DEFAULT_INPUT, help="Product feature CSV.")
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT, help="Output joblib model.")
    parser.add_argument("--trees", type=int, default=100, help="Number of RandomForest trees.")
    parser.add_argument("--max-depth", type=int, default=1, help="Maximum tree depth.")
    parser.add_argument("--test-size", type=float, default=0.25, help="Holdout evaluation fraction.")
    parser.add_argument("--seed", type=int, default=123, help="Random seed.")
    args = parser.parse_args()

    try:
        train_random_forest(
            csv_path=args.input,
            output_path=args.output,
            trees=args.trees,
            max_depth=args.max_depth,
            test_size=args.test_size,
            seed=args.seed,
        )
    except (OSError, ValueError) as error:
        parser.error(str(error))


if __name__ == "__main__":
    main()
