
# premi 안드로이드 앱 프로젝트

이 프로젝트는 Android 앱 + 별도 Node.js 서버로 구성됩니다.
앱은 서버에 메시지를 보내고, 서버가 OpenAI Responses API에 요청한 뒤 답변을 돌려줍니다.

## 먼저 알아둘 점
- 이 압축파일은 **APK가 아니라 APK를 빌드할 수 있는 프로젝트 소스**입니다.
- 실제 AI 사용에는 OpenAI API 계정/키와 사용량에 따른 비용이 발생할 수 있습니다.
- API 키는 앱 코드나 APK에 절대 넣지 마세요. 서버의 `OPENAI_API_KEY` 환경변수에만 저장해야 합니다.
- API 계정과 결제 설정은 보호자와 함께 확인하세요. 키를 채팅이나 공개 저장소에 올리지 마세요.

## 1. 서버 준비
1. Node.js 20 이상이 설치된 컴퓨터에서 `backend` 폴더를 엽니다.
2. 터미널에서 `npm install`을 실행합니다.
3. 서버 호스팅 서비스의 환경변수 설정에 `OPENAI_API_KEY`를 등록합니다.
4. 서버를 실행할 때 `npm start`를 사용합니다.
5. 배포된 HTTPS 주소를 확인합니다. 예: `https://premi-api.example.com`

서버를 인터넷에 공개하기 전에는 사용량 제한, 인증, 모니터링을 추가하는 것이 좋습니다.
이 예제는 개인 테스트용 최소 서버이며, 공개 배포용 보안/남용 방지 기능은 완성되어 있지 않습니다.

## 2. 앱을 서버에 연결
`app/build.gradle.kts`에서 아래 줄의 주소를 실제 HTTPS 서버 주소로 바꿉니다.

`buildConfigField("String", "SERVER_URL", "\\"https://YOUR-SERVER-DOMAIN\\"")`

예를 들어 서버 주소가 `https://premi-api.example.com`이면 해당 주소로 바꿉니다.

## 3. APK 빌드
1. Android Studio를 설치합니다.
2. Android Studio에서 이 프로젝트 폴더를 엽니다.
3. Gradle 동기화가 끝날 때까지 기다립니다.
4. 메뉴에서 **Build > Build Bundle(s) / APK(s) > Build APK(s)** 를 선택합니다.
5. 빌드가 성공하면 `app/build/outputs/apk/debug/app-debug.apk`가 생성됩니다.
6. APK를 본인 안드로이드 폰으로 옮겨 설치합니다. 테스트용 APK는 정식 스토어 배포용 서명 APK와 다릅니다.

## 주요 파일
- `app/src/main/java/com/premi/chat/MainActivity.kt`: 채팅 화면 및 서버 통신
- `backend/server.js`: OpenAI API 연결 서버
- `app/build.gradle.kts`: 앱 설정 및 서버 주소

## 참고
OpenAI API는 ChatGPT 앱 구독과 별도로 과금될 수 있습니다.
API 인증 키는 비밀정보이므로 Android 앱에 저장하지 않습니다.

