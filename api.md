# Oboetoki API 명세서 (최종 업데이트)

Base URL: `http://localhost:8000` (기본 설정)
Swagger UI: `http://localhost:8000/docs`

> **[주요 변경 사항]**
> - **인증 (Auth)**: `username` 필드가 삭제되고 **`email` 기반 로그인/가입**으로 전면 개편되었습니다. `name`, `phone`이 필수값으로 변경되었습니다.
> - **단어장 (Words)**: `memorization_level` 필드가 삭제되고, 일본어 요미가나/발음을 저장하는 **`reading` 필드가 추가**되었습니다. 정렬 파라미터(`sort_by`)가 제거되었습니다. 단어 추가 시 **중복 체크 로직**이 추가되었습니다.
> - **OCR (Vision)**: Tesseract 엔진이 제거되고, **OpenAI Vision LLM을 통해 이미지 파싱, 원본 언어 식별(`source_language`), 발음 추출(`reading`)**이 한 번에 자동 처리되도록 개편되었습니다.

---

## 1. 인증 (Auth)

### 1.1 회원가입
- **URL**: `/auth/signup`
- **Method**: `POST`
- **Description**: 새로운 사용자를 등록합니다. (**변경: username 삭제, email, name, phone 필수**)
- **Request Body** (`application/json`):
  ```json
  {
    "email": "user@example.com",
    "password": "securepassword123!",
    "name": "홍길동",
    "phone": "010-1234-5678"
  }
  ```
- **Response** (200 OK):
  ```json
  {
    "id": 1,
    "email": "user@example.com",
    "name": "홍길동",
    "phone": "010-1234-5678",
    "consecutive_attendance": 0,
    "last_login_date": null
  }
  ```

### 1.2 이메일 중복 확인 (**변경: /check-username -> /check-email**)
- **URL**: `/auth/check-email?email={email}`
- **Method**: `GET`
- **Description**: 입력한 이메일의 사용 가능 여부를 확인합니다.
- **Response** (200 OK):
  ```json
  {
    "is_available": true
  }
  ```

### 1.3 로그인 (**변경: email 기반 로그인**)
- **URL**: `/auth/login`
- **Method**: `POST`
- **Description**: 로그인 후 JWT Access Token을 발급받습니다. (폼 전송 시 username 필드에 email 주소 입력)
- **Request Body** (`application/x-www-form-urlencoded` - OAuth2 Password Form):
  - `username`: (문자열, 실제로는 이메일 주소 전송)
  - `password`: (문자열)
- **Response** (200 OK):
  ```json
  {
    "access_token": "eyJhbGciOiJIUz...",
    "token_type": "bearer"
  }
  ```

### 1.4 내 정보 조회
- **URL**: `/auth/me`
- **Method**: `GET`
- **Description**: 현재 로그인한 사용자의 이름과 이메일 정보를 반환합니다. (Authorization 헤더 필요)
- **Response** (200 OK):
  ```json
  {
    "name": "홍길동",
    "email": "user@example.com"
  }
  ```

---

*이하 모든 `/main` 및 `/words` API는 Request Header에 `Authorization: Bearer <access_token>`이 필요합니다.*

## 2. 메인 페이지 (Main)

### 2.1 메인 페이지 데이터 조회
- **URL**: `/main/`
- **Method**: `GET`
- **Description**: KST 자정 기준으로 출석을 갱신하고, 전체/외운 단어 통계와 LLM이 생성한 '오늘의 한 마디'를 반환합니다.
- **Response** (200 OK):
  ```json
  {
    "consecutive_attendance": 3,
    "total_words": 150,
    "memorized_words": 100,
    "quote_of_the_day": "꾸준함이 모든 것을 이깁니다. 오늘도 화이팅!"
  }
  ```

---

## 3. 단어장 (Words)

### 3.1 단어 목록 조회
- **URL**: `/words/`
- **Method**: `GET`
- **Query Parameters**:
  - `is_memorized` (boolean, 선택): 외운 단어(true)/못 외운 단어(false) 필터링
  - `sort_order` (string, 선택): 등록일자 정렬 방식 (`asc` 또는 `desc`, 기본값 `desc`)
- **Response** (200 OK):
  ```json
  [
    {
      "id": 1,
      "user_id": 1,
      "original_word": "暮らす",
      "reading": "くらす",
      "translated_word": "살다, 생활하다",
      "source_language": "ja",
      "is_memorized": false,
      "created_at": "2026-06-23T12:00:00Z"
    }
  ]
  ```

### 3.2 단일 단어 등록 (**추가됨: reading 필드**)
- **URL**: `/words/`
- **Method**: `POST`
- **Description**: 새로운 단어를 하나 등록합니다. 이미 존재하는 단어일 경우 409 에러를 반환합니다.
- **Request Body** (`application/json`):
  ```json
  {
    "original_word": "暮らす",
    "reading": "くらす",
    "translated_word": "살다, 생활하다",
    "source_language": "ja"
  }
  ```
- **Response** (200 OK): 등록된 단어 데이터 반환
- **Error Response** (409 Conflict): `{"detail": "Word already exists in your vocabulary"}`

### 3.3 단어 일괄 등록 (Batch)
- **URL**: `/words/batch`
- **Method**: `POST`
- **Description**: 여러 단어를 한 번에 등록합니다. (OCR 결과를 앱에서 승인 후 일괄 저장할 때 사용) 이미 존재하는 단어는 무시됩니다.
- **Request Body** (JSON Array):
  ```json
  [
    {
      "original_word": "暮らす",
      "reading": "くらす",
      "translated_word": "살다, 생활하다",
      "source_language": "ja"
    }
  ]
  ```
- **Response** (200 OK):
  ```json
  {
    "added_words": [
      {
        "id": 2,
        "user_id": 1,
        "original_word": "暮らす",
        "reading": "くらす",
        "translated_word": "살다, 생활하다",
        "source_language": "ja",
        "is_memorized": false,
        "created_at": "2026-06-23T12:05:00Z"
      }
    ],
    "ignored_words": ["이미_존재하는_단어"]
  }
  ```

### 3.4 단어 상세 조회
- **URL**: `/words/{word_id}`
- **Method**: `GET`
- **Description**: 특정 단어의 상세 정보를 반환합니다.
- **Response** (200 OK): 단일 단어 객체

### 3.5 단어 수정 (**삭제됨: memorization_level**)
- **URL**: `/words/{word_id}`
- **Method**: `PUT`
- **Description**: 기존 단어 정보를 수정합니다. 원어(`original_word`) 수정 시 이미 존재하는 단어일 경우 409 에러를 반환합니다.
- **Request Body** (`application/json`, 수정할 필드만 포함 가능):
  ```json
  {
    "original_word": "暮らす",
    "reading": "くらす",
    "translated_word": "살아 가다",
    "is_memorized": true
  }
  ```
- **Response** (200 OK): 수정된 단어 데이터 반환
- **Error Response** (409 Conflict): `{"detail": "Word already exists in your vocabulary"}`

### 3.6 단어 삭제
- **URL**: `/words/{word_id}`
- **Method**: `DELETE`
- **Description**: 단어 ID를 통해 특정 단어를 단어장에서 삭제합니다.
- **Response** (204 No Content): 반환 데이터 없음

### 3.7 OCR 단어 파싱 (DB 저장 X) (**변경: OpenAI Vision 적용 및 추출 필드 강화**)
- **URL**: `/words/ocr`
- **Method**: `POST`
- **Description**: 사진(이미지)을 업로드받아 LLM(OpenAI Vision) 모델을 통해 원어, 요미가나/발음, 한국어 뜻, 원본 언어 코드를 한 번에 추출합니다. DB에는 저장되지 않습니다.
- **Request Body** (`multipart/form-data`):
  - `file`: 사진 이미지 파일 (`.jpg`, `.png` 등)
- **Response** (200 OK):
  ```json
  {
    "parsed_words": [
      {
        "original": "暮らす",
        "reading": "くらす",
        "translated": "살다, 생활하다",
        "source_language": "ja"
      }
    ]
  }
  ```
