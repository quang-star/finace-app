# Refactor Graph

This file tracks the cleanup targets from the code audit so future searches have one stable entry point.

```mermaid
graph TD
    Audit["Code audit: redundancy and anti-patterns"]
    Audit --> Backend["Spring Boot backend"]
    Audit --> Android["Android app"]

    Backend --> B1["AI category lookup"]
    B1 --> B1a["AiScanController"]
    B1 --> B1c["CategoryService.findCategoryByKeyword(userId, keyword)"]

    Backend --> B2["Dead OCR parsing code"]
    B2 --> B2a["AiScanController private extractAmount/extractDate/extractMerchant"]
    B2 --> B2b["ReceiptParser is the source of truth"]

    Backend --> B3["Controller try/catch boilerplate"]
    B3 --> B3a["Future: GlobalExceptionHandler with @RestControllerAdvice"]

    Android --> A1["Date formatting"]
    A1 --> A1a["DateUtils API/date display helpers"]
    A1 --> A1b["Replace repeated SimpleDateFormat yyyy-MM-dd"]

    Android --> A2["LiveData lifecycle owners"]
    A2 --> A2a["Fragments should observe with getViewLifecycleOwner()"]
```

## Status

- Backend AI category lookup: done
- Backend dead OCR parsing code: done
- Backend global exception handling: started; AI controllers use centralized handling
- Android LiveData lifecycle owner: checked; current Fragment usages already use `getViewLifecycleOwner()`
- Android date formatting: done; repeated `SimpleDateFormat` usage is centralized in `DateUtils`
