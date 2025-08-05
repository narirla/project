import requests
import pandas as pd
from elasticsearch import Elasticsearch
from elasticsearch.helpers import bulk
import json
from datetime import datetime
import os
from bs4 import BeautifulSoup
import re
from datetime import datetime
import time
from elasticsearch.helpers import parallel_bulk


# Elasticsearch 연결 설정
es = Elasticsearch(["http://localhost:9200"])

numOfRows = 100
pageNo = 1
serviceKey = "KOStWAg%2Fl5tNd0YHoLgKl8bw7wJzGJWKOHUE3NOZs%2FhapIefgvXtfqQXpGX2wc0aA8wrJMTce4bos74uNqrPBg%3D%3D"
resultType = "json"

pageNo = 1
url = f"http://apis.data.go.kr/6260000/BusanFcltsDsgstInfoService/getFcltsDsgstInfo?ServiceKey={serviceKey}&pageNo={pageNo}&numOfRows={numOfRows}&resultType=JSON"


#  API에서 데이터를 다운로드하여 샘플 CSV로 저장
def download_pusan_facilities_data():
    """교통약자 API에서 데이터를 다운로드하여 CSV로 저장"""
    try:
        print("교통약자 API에서 데이터를 다운로드하는 중...")
        res = requests.get(url)

        # 응답 확인
        if res.status_code != 200:
            print(f"API 요청 실패: {res.status_code}")
            return None

        pusan_facilities_dict = res.json()  # json포맷 문자열 => dict로 변환

        # API 응답 구조 확인 및 수정
        if "response" not in pusan_facilities_dict:
            print("API 응답 구조가 예상과 다릅니다.")
            print(f"응답 키: {list(pusan_facilities_dict.keys())}")
            return None

        facilities_data = pusan_facilities_dict["response"]["body"]

        # 'item' 데이터 추출
        if "items" not in facilities_data:
            print("'item' 키를 찾을 수 없습니다.")
            print(f"getFoodKr 키: {list(facilities_data.keys())}")
            return None

        pusan_facilities_list = facilities_data["items"]["item"]

        # totalCount가 있다면 출력
        if "totalCount" in facilities_data:
            total_count = facilities_data["totalCount"]
            print(f"총 {total_count}개의 교통약자 편의시설 데이터를 가져왔습니다.")
        else:
            print(
                f"{len(pusan_facilities_list)}개의 교통약자 편의시설 데이터를 가져왔습니다."
            )

        # DataFrame으로 변환
        df = pd.DataFrame(pusan_facilities_list)

        # 샘플 CSV 파일 저장
        original_csv_path = "pusan_facilities_list_sample.csv"
        df.to_csv(original_csv_path, index=False, encoding="utf-8-sig")
        print(f"샘플 데이터가 '{original_csv_path}'에 저장되었습니다.")

        return original_csv_path

    except Exception as e:
        print(f"데이터 다운로드 중 오류 발생: {e}")
        return None


# 전체 페이지 데이터 가져오기
def download_all_pusan_facilities():
    """부산 교통약자 편의시설 API 전체 데이터를 순회하여 CSV 저장"""
    try:
        print("부산 교통약자 편의시설 전체 데이터를 다운로드하는 중...")

        service_key = "KOStWAg%2Fl5tNd0YHoLgKl8bw7wJzGJWKOHUE3NOZs%2FhapIefgvXtfqQXpGX2wc0aA8wrJMTce4bos74uNqrPBg%3D%3D"
        result_type = "json"
        base_url = "http://apis.data.go.kr/6260000/BusanFcltsDsgstInfoService/getFcltsDsgstInfo"
        num_of_rows = 100  # 한 페이지 최대 100건

        # 먼저 totalCount 가져오기
        first_page_url = f"{base_url}?serviceKey={service_key}&pageNo=1&numOfRows=1&resultType={result_type}"
        res = requests.get(first_page_url)
        total_count = res.json()["response"]["body"]["totalCount"]
        print(total_count + "개 가 나옵니다.")
        total_count = int(total_count)
        total_pages = (total_count // num_of_rows) + 1

        print(f"총 {total_count}건, 총 {total_pages}페이지")

        all_items = []

        for page_no in range(1, total_pages + 1):
            url = f"{base_url}?serviceKey={service_key}&pageNo={page_no}&numOfRows={num_of_rows}&resultType={result_type}"
            res = requests.get(url)
            data = res.json()
            items = (
                data.get("response", {})
                .get("body", [])
                .get("items", [])
                .get("item", [])
            )
            all_items.extend(items)
            print(f"{page_no}페이지 수집 완료. 누적: {len(all_items)}건")

        df = pd.DataFrame(all_items)

        csv_path = "pusan_facilities_data_all.csv"
        df.to_csv(csv_path, index=False, encoding="utf-8-sig")
        print(f"모든 데이터가 '{csv_path}'에 저장되었습니다.")

        return csv_path

    except Exception as e:
        print(f"전체 데이터 다운로드 실패: {e}")
        return None


# CSV 파일을 읽어서 contents 필드 각 항목 추출하여 새 필드로 추가한 전체 CSV 생성
def process_csv_detailed():
    """CSV 파일을 읽어서 contents 필드 각 항목 추출하여 새 필드로 추가한 CSV 생성"""

    # CSV 불러오기
    df = pd.read_csv("pusan_facilities_data_all.csv")

    def extract_info(contents_html):
        try:
            # HTML 텍스트 파싱
            soup = BeautifulSoup(contents_html, "html.parser")
            text = soup.get_text(separator="\n")

            # 값만 추출
            store_name = phone = address = location = table_info = menu = date = (
                source_org
            ) = source_tel = homepage = None

            match_name_tel = re.search(r"상\s*호.*?:\s*(.+?)\s*\(([\d\-]+)\)", text)
            if match_name_tel:
                store_name = match_name_tel.group(1).strip()
                phone = match_name_tel.group(2).strip()

            match_address = re.search(r"주\s*소\s*:\s*(.+)", text)
            if match_address:
                address = match_address.group(1).strip()

            match_location = re.search(r"위\s*치\s*:\s*(.+)", text)
            if match_location:
                location = match_location.group(1).strip()

            match_tables = re.search(r"테이블 수\s*:\s*(.+)", text)
            if match_tables:
                table_info = match_tables.group(1).strip()

            match_menu = re.search(r"주메뉴\s*:\s*(.+)", text)
            if match_menu:
                menu = match_menu.group(1).strip()

            match_date = re.search(r"(\d{4}년\s*\d{1,2}월\s*\d{1,2}일 기준)", text)
            if match_date:
                date = match_date.group(1).strip()

            match_source = re.search(r"출처\s*:\s*(.*?)\(T:([\d\-]+)\)", text)
            if match_source:
                source_org = match_source.group(1).strip()
                source_tel = match_source.group(2).strip()

            match_url = re.search(r"홈페이지:\s*(\S+)", text)
            if match_url:
                homepage = match_url.group(1).strip()

            return pd.Series(
                [
                    store_name,
                    phone,
                    address,
                    location,
                    table_info,
                    menu,
                    date,
                    source_org,
                    source_tel,
                    homepage,
                ]
            )
        except Exception:
            return pd.Series([None] * 10)

    # 새 컬럼 생성
    df[
        [
            "store_name",
            "phone",
            "address",
            "location",
            "table_info",
            "menu",
            "date",
            "source_org",
            "source_tel",
            "homepage",
        ]
    ] = df["contents"].apply(extract_info)

    # contents 컬럼 제거
    df.drop(columns=["contents"], inplace=True)

    processed_csv_path = "pusan_facilities_extracted.csv"

    # 필요한 컬럼만 저장
    df.to_csv(processed_csv_path, index=False)

    return processed_csv_path


# 주소 위경도 추출
def process_csv_lonlat():
    # 1. 발급받은 카카오 REST API 키 입력
    KAKAO_API_KEY = "1bf157c22ef312c3e554d7f6a5816b81"

    # 2. 주소 기반 위경도 반환 함수
    def get_lat_lng(address):
        url = "https://dapi.kakao.com/v2/local/search/address.json"
        headers = {"Authorization": f"KakaoAK {KAKAO_API_KEY}"}
        params = {"query": address}
        try:
            response = requests.get(url, headers=headers, params=params)
            if response.status_code == 200:
                result = response.json()
                if result["documents"]:
                    lat = result["documents"][0]["y"]
                    lng = result["documents"][0]["x"]
                    return lat, lng
            return None, None
        except Exception as e:
            print(f"Error for address '{address}': {e}")
            return None, None

    # 3. CSV 파일 불러오기
    # df = pd.read_csv()

    df = pd.read_csv("pusan_facilities_extracted.csv")

    # 4. 위경도 추출 (시간 지연 포함)
    latitudes = []
    longitudes = []

    for idx, row in df.iterrows():
        address = row["address"]
        lat, lng = get_lat_lng(address)
        latitudes.append(lat)
        longitudes.append(lng)
        print(f"[{idx+1}/{len(df)}] {address} → lat: {lat}, lng: {lng}")
        # time.sleep(0.1)  # 카카오 API 호출 제한(초당 10회) 방지

    # 5. 결과 추가
    df["lat"] = latitudes
    df["lng"] = longitudes

    # 6. 새 CSV 저장
    processed_csv_path = "pusan_facilities_with_coords.csv"
    df.to_csv(processed_csv_path, index=False)

    return processed_csv_path


# CSV 파일을 읽어서 location 필드를 추가하고 가공된 CSV 생성
def process_csv_with_location(csv_path):
    """CSV 파일을 읽어서 location 필드를 추가하고 가공된 CSV 생성"""
    try:
        print(f"'{csv_path}' 파일을 읽어서 전처리하는 중...")

        # CSV 파일 읽기
        df = pd.read_csv(csv_path, encoding="utf-8-sig")

        # 컬럼명 확인 및 대소문자 통일
        print(f"CSV 컬럼: {list(df.columns)}")

        # location 필드 생성 (pandas 벡터화 방식으로 처리)
        def make_location(row):
            try:
                # 컬럼명이 대문자일 수 있으므로 확인
                lat_col = "LAT" if "LAT" in df.columns else "lat"
                lng_col = "LNG" if "LNG" in df.columns else "lng"

                if lat_col not in df.columns or lng_col not in df.columns:
                    return ""

                map_x = float(row[lat_col])  # 위도
                map_y = float(row[lng_col])  # 경도
                # Elasticsearch geo_point는 "lat,lon" 형식
                return f"{map_x},{map_y}"
            except (ValueError, TypeError, KeyError):
                return ""

        df["location"] = df.apply(make_location, axis=1)
        valid_coordinates = (df["location"] != "").sum()

        # 가공된 CSV 파일 저장
        processed_csv_path = "pusan_facilities_data_processed.csv"
        df.to_csv(processed_csv_path, index=False, encoding="utf-8-sig")

        print(f"전처리 완료: {valid_coordinates}개 유효한 좌표, {len(df)}개 총 데이터")
        print(f"가공된 데이터가 '{processed_csv_path}'에 저장되었습니다.")

        return processed_csv_path

    except Exception as e:
        print(f"CSV 전처리 중 오류 발생: {e}")
        return None


# pusan_facilities 인덱스 생성 (기존 인덱스가 있으면 삭제 후 재생성)
def create_pusan_facilities_index():
    """pusan_facilities 인덱스 생성"""
    index_name = "pusan_facilities"

    # 기존 인덱스가 있으면 삭제
    if es.indices.exists(index=index_name):
        print(f"기존 인덱스 '{index_name}'를 삭제합니다.")
        es.indices.delete(index=index_name)

    # 인덱스 매핑 설정 (부산 교통약자 편의시설 API 필드에 맞게 수정)
    mapping = {
        "mappings": {
                "properties": {
                    "@timestamp": {"type": "date"},
                    "address": {"type": "text"},
                    "boardCode": {"type": "keyword"},
                    "boardCodeNm": {"type": "keyword"},
                    "date": {"type": "keyword"},
                    "gubun": {"type": "keyword"},
                    "homepage": {"type": "keyword"},
                    "imgUrl": {"type": "keyword"},
                    "lat": {"type": "double"},
                    "lng": {"type": "double"},
                    "location": {"type": "geo_point"},
                    "menu": {"type": "keyword"},
                    "phone": {"type": "keyword"},
                    "registerDate": {"type": "date", "format": "iso8601"},
                    "setValue": {"type": "keyword"},
                    "setValueNm": {"type": "keyword"},
                    "store_name": {"type": "keyword"},
                    "subject": {"type": "keyword"},
                    "table_info": {"type": "text"},
            }
        }
    }

    # 인덱스 생성
    es.indices.create(index=index_name, body=mapping)
    print(f"인덱스 '{index_name}'가 성공적으로 생성되었습니다.")


# 간단한 데이터 클리닝 함수 (최소한의 처리만)
def clean_record(record):
    """레코드 데이터 클리닝 - 결측치 처리 중심"""
    cleaned_record = {}

    for key, value in record.items():
        # NaN이나 None 값을 빈 문자열로 처리
        if pd.isna(value) or value is None:
            cleaned_record[key] = ""
        else:
            cleaned_record[key] = value

    return cleaned_record


# 가공된 CSV 파일을 읽어서 Elasticsearch에 저장 (elasticsearch.helpers.bulk 사용)
def load_csv_to_elasticsearch(csv_path):
    """가공된 CSV 파일을 읽어서 Elasticsearch에 저장"""
    try:
        print(f"'{csv_path}' 파일을 읽어서 Elasticsearch에 저장하는 중...")

        # CSV 파일 읽기
        df = pd.read_csv(csv_path, encoding="utf-8-sig")
        print(f"CSV에서 {len(df)}개 레코드를 읽었습니다.")

        # 데이터 저장 시간 추가
        df["data_stored_at"] = datetime.now().isoformat()
        print("데이터 저장 시간 추가 완료")

        # DataFrame을 딕셔너리 리스트로 변환
        records = df.to_dict("records")
        print("딕셔너리 리스트로 변환 완료")

        index_name = "pusan_facilities"  # 올바른 인덱스명 사용

        # 벌크 인덱싱을 위한 문서 생성 함수
        def doc_generator():
            for i, record in enumerate(records):
                # 데이터 클리닝
                cleaned_record = clean_record(record)

                # location 필드가 있는 경우 geo_point 형식으로 변환
                if "location" in cleaned_record and cleaned_record["location"]:
                    try:
                        lat, lon = cleaned_record["location"].split(",")
                        lat, lon = float(lat), float(lon)
                        # 유효한 좌표인지 확인
                        if -90 <= lat <= 90 and -180 <= lon <= 180:
                            cleaned_record["location"] = {"lat": lat, "lon": lon}
                        else:
                            cleaned_record.pop("location", None)
                    except (ValueError, AttributeError):
                        # location 필드가 유효하지 않으면 제거
                        cleaned_record.pop("location", None)

                yield {
                    "_index": index_name,
                    "_id": i + 1,  # 고유 ID 할당
                    "_source": cleaned_record,
                }
            print("데이터 클리닝 완료")

        # 벌크 인덱싱 실행
        success_count = 0
        error_count = 0

        try:
            # bulk helper 사용
            success_count = 0
            fail_count = 0
            failed_docs = []

            for ok, result in parallel_bulk(es, doc_generator(), chunk_size=100):
                if ok:
                    success_count += 1
                else:
                    fail_count += 1
                    failed_docs.append(result)

            print(f"성공: {success_count}, 실패: {fail_count}")

        except Exception as e:
            print(f"벌크 인덱싱 중 오류 발생: {e}")

        print(f"인덱싱 완료: 성공 {success_count}개, 실패 {error_count}개")

        # 인덱스 새로고침
        es.indices.refresh(index=index_name)
        print("인덱스 새로고침 완료")

        # 저장된 문서 수 확인
        count_result = es.count(index=index_name)
        print(f"Elasticsearch에 저장된 문서 수: {count_result['count']}개")


    except Exception as e:
        print(f"CSV를 Elasticsearch에 저장하는 중 오류 발생: {e}")


# 인덱스 통계 조회
def get_index_stats():
    """인덱스 통계 조회"""
    index_name = "pusan_facilities"  # 올바른 인덱스명 사용

    try:
        # 문서 수 조회
        count_result = es.count(index=index_name)
        print(f"총 문서 수: {count_result['count']}개")

        # 인덱스 정보 조회
        stats = es.indices.stats(index=index_name)
        print(
            f"인덱스 크기: {stats['indices'][index_name]['total']['store']['size_in_bytes']} bytes"
        )

    except Exception as e:
        print(f"인덱스 통계 조회 중 오류 발생: {e}")


# 메인 함수
def main():
    """메인 함수"""
    print("부산맛집 데이터 Elasticsearch 저장 프로그램 (수정버전)")
    print("=" * 60)

    # Elasticsearch 연결 확인
    try:
        if es.ping():
            print("✓ Elasticsearch 연결 성공")
        else:
            print("✗ Elasticsearch 연결 실패")
            return
    except Exception as e:
        print(f"✗ Elasticsearch 연결 오류: {e}")
        print("Elasticsearch가 실행 중인지 확인해주세요.")
        return

    # 1단계: API에서 데이터 다운로드
    print("\n[1단계] API에서 데이터 다운로드")
    original_csv_path = download_pusan_facilities_data()
    if not original_csv_path:
        print("✗ 데이터 다운로드 실패")
        return

    # 1.5단계: 전체 데이터 가져오기
    print("\n[1.5단계] 전체 데이터 가져오기")
    download_all_pusan_facilities()

    # 2단계: CSV 전처리 (contents 필드 나누기)
    print("\n[2단계] CSV 전처리")
    processed_csv_path = process_csv_detailed()

    # 3단계: Elasticsearch 인덱스 생성
    print("\n[3단계] Elasticsearch 인덱스 생성")
    create_pusan_facilities_index()

    # 4단계: 가공된 CSV를 Elasticsearch에 저장
    print("\n[4단계] Elasticsearch에 데이터 저장")
    coords = process_csv_lonlat()
    processed_csv_path = process_csv_with_location(coords)
    load_csv_to_elasticsearch(processed_csv_path)

    # 5단계: 인덱스 통계 확인
    print("\n[5단계] 인덱스 통계 확인")
    get_index_stats()

    print("\n" + "=" * 60)
    print("✓ 프로그램 완료!")


if __name__ == "__main__":
    main()
