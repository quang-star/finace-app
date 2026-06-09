import argparse
from pathlib import Path

try:
    import joblib
    import m2cgen as m2c
except ModuleNotFoundError as error:
    raise SystemExit(
        f"Missing Python package '{error.name}'. Install export dependencies with:\n"
        "python -m pip install -r tools/requirements-product-random-forest.txt"
    ) from error


REPO_ROOT = Path(__file__).resolve().parent.parent
DEFAULT_INPUT = REPO_ROOT / "tools" / "product_random_forest.joblib"
DEFAULT_OUTPUT = (
    REPO_ROOT
    / "PersonalFinanceApp"
    / "app"
    / "src"
    / "main"
    / "java"
    / "com"
    / "example"
    / "personalfinance"
    / "ml"
    / "ProductRandomForestModel.java"
)
DEFAULT_PACKAGE = "com.example.personalfinance.ml"
DEFAULT_CLASS_NAME = "ProductRandomForestModel"


def java_string_array(values: list[str]) -> str:
    escaped = [value.replace("\\", "\\\\").replace('"', '\\"') for value in values]
    return "new String[] {" + ", ".join(f'"{value}"' for value in escaped) + "}"


def java_int_array(values: list[int]) -> str:
    return "new int[] {" + ", ".join(str(int(value)) for value in values) + "}"


def inject_metadata(
    java_code: str,
    category_ids: list[int],
    category_names: list[str],
    feature_names: list[str],
) -> str:
    metadata = (
        f"    public static final int[] CATEGORY_IDS = {java_int_array(category_ids)};\n"
        f"    public static final String[] CATEGORY_NAMES = {java_string_array(category_names)};\n"
        f"    public static final String[] FEATURE_NAMES = {java_string_array(feature_names)};\n\n"
        "    public static int predictCategoryId(double[] features) {\n"
        "        return CATEGORY_IDS[bestCategoryIndex(features)];\n"
        "    }\n\n"
        "    public static String predictCategoryName(double[] features) {\n"
        "        return CATEGORY_NAMES[bestCategoryIndex(features)];\n"
        "    }\n\n"
        "    public static double predictConfidence(double[] features) {\n"
        "        double[] scores = score(features);\n"
        "        return scores[bestScoreIndex(scores)];\n"
        "    }\n\n"
        "    private static int bestCategoryIndex(double[] features) {\n"
        "        return bestScoreIndex(score(features));\n"
        "    }\n\n"
        "    private static int bestScoreIndex(double[] scores) {\n"
        "        int bestIndex = 0;\n"
        "        for (int i = 1; i < scores.length; i++) {\n"
        "            if (scores[i] > scores[bestIndex]) {\n"
        "                bestIndex = i;\n"
        "            }\n"
        "        }\n"
        "        return bestIndex;\n"
        "    }\n\n"
    )
    return java_code.replace("{\n", "{\n" + metadata, 1)


def export_java(input_path: Path, output_path: Path, package_name: str, class_name: str) -> None:
    payload = joblib.load(input_path)
    model = payload["model"]
    feature_names = list(payload["feature_names"])
    category_names_by_id = payload["category_names"]
    category_ids = [int(value) for value in model.classes_.tolist()]
    category_names = [category_names_by_id[category_id] for category_id in category_ids]

    java_code = m2c.export_to_java(model, class_name=class_name)
    java_code = inject_metadata(java_code, category_ids, category_names, feature_names)
    java_code = (
        f"package {package_name};\n\n"
        "// Generated from tools/product_random_forest.joblib. Do not edit by hand.\n"
        + java_code
    )

    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(java_code, encoding="utf-8")
    print(f"saved={output_path}")


def main() -> None:
    parser = argparse.ArgumentParser(description="Export a joblib RandomForest model to Java.")
    parser.add_argument("--input", type=Path, default=DEFAULT_INPUT, help="Input joblib model.")
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT, help="Output Java model.")
    parser.add_argument("--package", default=DEFAULT_PACKAGE, help="Generated Java package.")
    parser.add_argument("--class-name", default=DEFAULT_CLASS_NAME, help="Generated Java class name.")
    args = parser.parse_args()

    try:
        export_java(args.input, args.output, args.package, args.class_name)
    except (KeyError, OSError, ValueError) as error:
        parser.error(str(error))


if __name__ == "__main__":
    main()
