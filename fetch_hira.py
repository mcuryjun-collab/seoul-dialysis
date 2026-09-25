"""
HIRA 병원평가정보서비스 - 혈액투석 1등급 실데이터 가져와서 hospitals.json 보강하는 스크립트
사용법:
1. data.go.kr 에서 "건강보험심사평가원_병원평가정보서비스" 검색 -> 활용신청 -> serviceKey 발급 (일반인증키 Decrypt 버전)
2. export DATA_GO_KR_KEY="발급받은 키"
3. python fetch_hira.py

결과: hospitals_real.json 생성 -> 기존 hospitals.json과 merge해서 hdf/night 정보 유지
"""
import requests, json, pathlib, os, time

SERVICE_KEY = os.environ.get("DATA_GO_KR_KEY", "")
if not SERVICE_KEY:
    print("DATA_GO_KR_KEY 환경변수가 없습니다. 예: export DATA_GO_KR_KEY='YOUR_KEY'")
    print("키 없이도 기존 hospitals.json 198곳 데이터로 앱은 동작합니다.")
    exit(0)

BASE = "https://apis.data.go.kr/B551182/hospAsmRstInfoService/getHospAsmRstInfo"
params = {
    "serviceKey": SERVICE_KEY,
    "numOfRows": 200,
    "pageNo": 1,
    "asmGubun": "03", # 03 = 혈액투석
    "_type": "json"
}

all_items = []
for page in range(1, 6): # 최대 1000개
    params["pageNo"] = page
    r = requests.get(BASE, params=params, timeout=20)
    print(f"page {page} status {r.status_code}")
    try:
        j = r.json()
        items = j.get("response", {}).get("body", {}).get("items", {}).get("item", [])
        if not items:
            break
        if isinstance(items, dict):
            items = [items]
        all_items.extend(items)
        time.sleep(0.3)
    except Exception as e:
        print("parse error", e, r.text[:500])
        break

print(f"총 {len(all_items)}개 수집")
# 서울만 필터
seoul = [x for x in all_items if "서울" in (x.get("addr") or "")]
print(f"서울 {len(seoul)}개")

# 저장
out_path = pathlib.Path(__file__).parent / "app" / "src" / "main" / "assets" / "hospitals_real_hira.json"
with open(out_path, "w", encoding="utf-8") as f:
    json.dump(seoul, f, ensure_ascii=False, indent=2)
print(f"saved to {out_path}")

# 기존 로컬 데이터와 병합 예시: 이름 매칭해서 grade 업데이트
local_path = pathlib.Path(__file__).parent / "app" / "src" / "main" / "assets" / "hospitals.json"
with open(local_path, encoding="utf-8") as f:
    local = json.load(f)

name_to_hira = {h.get("yadmNm",""): h for h in seoul}
merged = 0
for h in local:
    # 이름 유사 매칭 (간단)
    for hira_name, hira in name_to_hira.items():
        if h["name"] in hira_name or hira_name in h["name"]:
            h["grade"] = f"{hira.get('asmGrd','1')}등급"
            merged += 1
            break

print(f"merged {merged} grades")
with open(local_path, "w", encoding="utf-8") as f:
    json.dump(local, f, ensure_ascii=False, indent=2)
