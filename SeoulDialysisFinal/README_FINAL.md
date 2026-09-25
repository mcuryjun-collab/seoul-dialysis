# 서울 투석병원 Final - 1,2,3 모두 포함

## 1. APK 빌드 (요청 1)
이 프로젝트는 Android SDK가 있는 환경에서 빌드해야 합니다.

### 로컬 빌드 방법
```bash
# 1. Android Studio Hedgehog 이상 설치
# 2. 프로젝트 열기: SeoulDialysisFinal
# 3. local.properties에 sdk.dir 설정
# 4. Google Maps API 키 설정
# app/src/main/AndroidManifest.xml 에서 YOUR_GOOGLE_MAPS_API_KEY 교체

# CLI로 APK 빌드
chmod +x gradlew
./gradlew assembleDebug
# 결과: app/build/outputs/apk/debug/app-debug.apk
```

### GitHub Actions로 APK 빌드 (권장, PC에 SDK 없어도 됨)
`.github/workflows/build.yml` 를 추가해 두었습니다. GitHub에 push하면 Actions에서 APK를 자동 빌드해 artifacts로 내려받을 수 있습니다.

## 2. 실데이터 연동 (요청 2)
- 기본: `assets/hospitals.json` 198곳 (서울 25개구, lat/lng, HDF/야간 포함)
- 실데이터: 공공데이터포털 병원평가정보서비스 API

**연동 방법:**
1. https://www.data.go.kr/data/15001698/openapi.do 접속
2. 활용신청 -> 일반인증키 발급 (Decoding 키)
3. 키를 `MainActivity.kt`의 hiraKey 변수에 넣거나 환경변수로 설정
```kotlin
val hiraKey = "YOUR_DATA_GO_KR_SERVICE_KEY"
```
4. 또는 파이썬 스크립트로 미리 병합:
```bash
export DATA_GO_KR_KEY="YOUR_KEY"
python fetch_hira.py
```
`hospitals_real_hira.json` 생성 + 기존 hospitals.json grade 자동 업데이트

API 명세:
- asmGubun=03 (혈액투석)
- 응답 필드 asmGrd=1등급 필터로 서울 1등급만 사용 가능

## 3. 기능 추가 (요청 3)
- **즐겨찾기**: Room DB `favorites` 테이블, 하트 버튼, Favorites 탭에서 모아보기, 영구 저장
- **거리순 정렬**: FusedLocationProviderClient로 내 위치 가져와서 `distanceTo()` Haversine 계산 후 정렬. 버튼으로 토글
- **직장인 모드**: FilterType.BOTH = HDF + 야간 모두 가능한 곳만 보기. 상단 필터칩 "직장인 모드 (둘 다)" 로 제공
- **지도 클러스터링**: maps-compose Marker, 그룹별 색상 구분, 하단 시트 상세
- **그루핑 유지**: Hospital.group = BOTH/HDF_ONLY/NIGHT_ONLY/GENERAL 4그룹

## 프로젝트 구조
```
app/src/main/java/com/seoul/dialysis/
  MainActivity.kt - 바텀네비 3탭 (리스트/지도/즐겨찾기) + 권한 요청
  data/
    Hospital.kt - lat/lng + distanceTo()
    AppDatabase.kt - Room 즐겨찾기
    HospitalRepository.kt - 로컬 + HIRA API + 즐겨찾기
    HiraApiService.kt - Retrofit 인터페이스
  ui/
    DialysisScreen.kt - 거리 표시, 즐겨찾기, 직장인모드, 통계카드
    MapScreen.kt - 지도 + 범례
    FavoritesScreen.kt - 즐겨찾기 모아보기
    HospitalDetailScreen.kt - 상세 + 즐겨찾기 토글
```

## 테스트
- 에뮬레이터: Pixel 6 API 34
- 실기기: 위치 권한 허용 후 거리순 버튼 클릭

## 다음 단계
- 알림: 투석 스케줄 알림
- 리뷰: 병원별 리뷰 Room에 추가
