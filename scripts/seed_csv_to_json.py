"""
시드 CSV(구글 시트에서 내려받은 것) → 서버가 읽는 JSON 으로 변환.

사용법 (프로젝트 루트에서)
    python3 scripts/seed_csv_to_json.py 시트_용어.csv 시트_문제.csv

결과: src/main/resources/seed/seed_terms.json, seed_questions.json
서버가 켜질 때 question.seed.SeedLoader 가 이 두 파일을 읽어 DB 에 넣는다 (같은 id 는 덮어쓰기).
검수용 칸(검수자, 상태, 코멘트)은 버린다. 상태가 "삭제"인 줄은 넣지 않는다.
시사 퀴즈(purpose = NEWS)는 publishDate 칸에 게시일(2026-11-23 형식)을 적는다. sector, stage 는 비운다.
"""
import csv
import json
import sys
from pathlib import Path

OUT = Path(__file__).resolve().parent.parent / "src/main/resources/seed"


def rows(path):
    with open(path, encoding="utf-8-sig", newline="") as f:
        for row in csv.DictReader(f):
            if not (row.get("id") or "").strip():
                continue
            if (row.get("상태") or "").strip() == "삭제":
                continue
            yield {k.strip(): (v or "").strip() for k, v in row.items() if k}


def json_cell(value, default=None):
    return json.loads(value) if value else default


def blank_to_none(value):
    return value or None


def convert_terms(path):
    return [{
        "id": int(r["id"]),
        "name": r["name"],
        "sector": int(r["sector"]) if r.get("sector") else None,
        "stage": int(r["stage"]) if r.get("stage") else None,
        "definition": r["definition"],
        "easy": r["easy"],
        "example": blank_to_none(r.get("example")),
        "related": json_cell(r.get("related"), []),
        "source": blank_to_none(r.get("source")),
    } for r in rows(path)]


def convert_questions(path):
    return [{
        "id": int(r["id"]),
        "code": blank_to_none(r.get("code")),
        "purpose": r["purpose"],
        "sector": int(r["sector"]) if r.get("sector") else None,
        "stage": int(r["stage"]) if r.get("stage") else None,
        "type": r["type"],
        "termIds": json_cell(r.get("termIds"), []),
        "prompt": r["prompt"],
        "options": json_cell(r.get("options")),
        "answer": json_cell(r["answer"]),
        "explanation": r["explanation"],
        "source": blank_to_none(r.get("source")),
        "publishDate": blank_to_none(r.get("publishDate")),
    } for r in rows(path)]


def write(name, data):
    OUT.mkdir(parents=True, exist_ok=True)
    (OUT / name).write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"{name}: {len(data)}개")


if __name__ == "__main__":
    if len(sys.argv) != 3:
        sys.exit("사용법: python3 scripts/seed_csv_to_json.py 용어.csv 문제.csv")
    write("seed_terms.json", convert_terms(sys.argv[1]))
    write("seed_questions.json", convert_questions(sys.argv[2]))
