package com.example.passionDaily

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.passionDaily.navigation.SetupNavigation
import com.example.passionDaily.ui.theme.Passion_DailyTheme
import com.example.passionDaily.util.QuoteCategory
import com.example.passionDaily.util.QuoteCreator
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
//    private val db = FirebaseFirestore.getInstance()
//    private val quoteCreator = QuoteCreator(db)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Passion_DailyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    SetupNavigation(navController = navController)
                }
            }
        }
        /**
         * 명언 추가 시 사용
         */
//        lifecycleScope.launch {
//            // exercise
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.EXERCISE,
//                text = "일단 규칙적으로 운동하면, 이것을 멈추기가 어려울 것이다.",
//                person = "에린 그레이"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.EXERCISE,
//                text = "운동을 위해 시간을 내지 않으면, 병 때문에 시간을 내야 될지도 모른다.",
//                person = "로빈 샤르마"
//            )
//            // effort
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.EFFORT,
//                text = "진부한 말 같지만 성공하려면 남들보다 두 배로 노력해라.",
//                person = "오스카 델 라 호야"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.EFFORT,
//                text = "내가 해줄 수 있는 얘기는 이거 밖에 없다.",
//                person = "코비 브라이언트"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.EFFORT,
//                text = "당신이 포기하는 순간은 다른 사람이 이기도록 내버려 두는 순간입니다.",
//                person = "르브론 제임스"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.EFFORT,
//                text = "피곤함이란, 아무 의미가 없는 것이다. 피곤함은 오직 생각에서 존재한다. 만약 당신 스스로 피곤하다고 말한다면 당신은 피곤할 것이다. 나는 피곤해지지 않는다.",
//                person = "마이클 펠프스"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.EFFORT,
//                text = "최고가 되고싶다면, 다른 이들이 하고싶지 않아 하는 일을 해야한다.",
//                person = "크리스티아누 호날두"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.EFFORT,
//                text = "벌써 완벽하다고 생각한다면, 당신은 절대 완벽해질 수 없을 것이다.",
//                person = "카를로스 푸욜"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.EFFORT,
//                text = "힘든가? 오늘 쉬면 내일은 뛰어야 한다.",
//                person = "마이클 조던"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.EFFORT,
//                text = "성공을 경험하기 위해선, 실패를 경험해보아야 한다",
//                person = "오스카 와일드"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.EFFORT,
//                text = "괴로운 시련처럼 보이는 것이 뜻밖의 좋은 일일 때가 많다.",
//                person = "앙리 드 몽테를랑"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.EFFORT,
//                text = "평범한 인간이 이따금 비상한 결의로 성공하는 경우가 있는데, 그것은 불안에서 벗어나려고 끊임없이 노력한 결과이다.",
//                person = "크리스티아누 호날두"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.EFFORT,
//                text = "모두가 크리스티아누가 되고 싶어 해… 내가 너에게 그 길을 알려줄게, 너는 그걸 할 수 있을 거라고 생각해? 진정으로 위대해지고 싶다면, 스스로를 훈련할 준비가 되어 있어야 해. 너의 과정에 헌신해야 하고, 그것에 생명이 걸린 것처럼 고수해야 해.",
//                person = "인드라 누이"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.EFFORT,
//                text = "두 배로 생각하라 두 배로 노력하라 그것이 가진 것 없는 보통 사람이 성공하는 비결이다.",
//                person = "히포크라테스"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.EFFORT,
//                text = "허송세월하며 할 일이 없는 사람은 악으로 끌려가는 것이 아니라 저절로 기울어진다.",
//                person = "이순신"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.EFFORT,
//                text = "살려고 하면 죽을 것이요, 죽고자 하면 살 것이다. 길목을 막으면 한 명이 능히 천명을 감당할 수 있다.",
//                person = "샤를 드골"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.EFFORT,
//                text = "우리는 전투에서 졌지만, 전쟁에는 아직 지지 않았다.",
//                person = "플로이드 메이웨더"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.EFFORT,
//                text = "나를 건방지고 재미없는 복서라고 욕해도 좋아. 언제나 앞에서는 까불거리고 천진난만하지만, 난 내일이 오기가 두려울 정도로 끔찍한 훈련을 하고 있어. 난 천재가 아니야 진짜로. 내 노력만큼은 인정해줘. 한 대라도 좀 맞춰봐",
//                person = "코너 맥그리거"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.EFFORT,
//                text = "당신이 머릿 속으로 볼 수 있고 입 밖으로 꺼낼 용기가 있다면 그것은 반드시 현실이 될 것이다.",
//                person = "정주영"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.EFFORT,
//                text = "길이 어디있는지 모른다면 길을 직접 찾으면 되고, 길이 보이지 않는다면 직접 길을 닦으면 되지",
//                person = "이소룡"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.EFFORT,
//                text = "나는 만가지 발차기를 하는 사람을 두려워하지 않지만, 하나의 발차기를 만 번 연습한 사람은 두려워 한다.",
//                person = "제프 베조스"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.EFFORT,
//                text = "스트레스는 아무것도 하지 않을 때 나타납니다.",
//                person = "데이나 화이트"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.EFFORT,
//                text = "지금 세대에는 마인드가 쓰레기인 사람들로 가득해. 그렇기에 진짜 야망 있는 소수의 사람들은 이런 꼬맹이들 쯤은 전부 밟고 지나갈 수 있어. 지금처럼 이렇게까지 기회가 많았던 적이 없어.",
//                person = "안성재"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.EFFORT,
//                text = "노력해서도 안 되는 것들이 있어요. 근데 그걸 되게 만드는 게 중요한 것 같아요. 노력해서 안되니까 '나는 노력했어'라고 단정을 지으시는 분들이 많은데, 어떠한 수단과 방법을 가리지 않고 이것을 만들어 내는 것이 진정한 노력이라고 생각합니다.",
//                person = "nas"
//            )
//
//// creativity
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.CREATIVITY,
//                text = "모두가 비슷한 생각을 한다는 것은 아무도 생각하고 있지 않다는 말이다.",
//                person = "Einstein"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.CREATIVITY,
//                text = "창의적인 사람들의 성격을 한마디로 표현하면 복합성이다.",
//                person = "미하이 칙센트미하이"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.CREATIVITY,
//                text = "다른 누군가가 되기를 원하는 것은 자신을 버리는 것이다",
//                person = "커트 코베인"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.CREATIVITY,
//                text = "다른 누군가가 되어서 사랑받기보다는 있는 그대로의 나로서 미움받는 것이 낫다",
//                person = "커트 코베인"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.CREATIVITY,
//                text = "상상할 수 있는 모든 것은 실제이다.",
//                person = "파블로 피카소"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.CREATIVITY,
//                text = "실수도 좋다. 실수는 재미있는 것들을 만들어낸다. 창의성에는 그 어떤 한계도 없다.",
//                person = "헨릭 빕스코브"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.CREATIVITY,
//                text = "이전에 관련이 없던 아이디어와 개념, 데이터와 지식을 새로운 방식으로 결합할 때 상상력과 창의력이 생겨난다.",
//                person = "앨빈 토플러"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.CREATIVITY,
//                text = "모든 사람은 창의적이다. 그러나 익숙한 것에 머물러 있는 동안은 혁신적인 아이디어가 자라지 않는다. 항상 해오던 일을 하면 항상 얻던 것만 얻을 수 있다.",
//                person = "프랜시스 베이컨"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.CREATIVITY,
//                text = "나의 모든 동작은 훌륭한 선배로부터 훔친 것이다.",
//                person = "코비 브라이언트"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.CREATIVITY,
//                text = "항상 갈구하라. 바보짓을 두려워 하지 말라.",
//                person = "스티브 잡스"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.CREATIVITY,
//                text = "혁신은 1000번 '아니오'라고 말하는 것에서 시작된다.",
//                person = "스티브 잡스"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.CREATIVITY,
//                text = "비판을 피하기만 한다면, 착한 척 하기 위해서 새로운 시도를 절대 할 수 없습니다.",
//                person = "제프 베조스"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.CREATIVITY,
//                text = "가장 개인적인 것이 가장 창의적인 것이다",
//                person = "마틴 스콜세지"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.CREATIVITY,
//                text = "온갖 삶에 대한 호기심이 위대한 창의적인 사람들의 비밀이라고 생각한다.",
//                person = "레오 버넷"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.CREATIVITY,
//                text = "우리는 천재지만, 학교와 시스템에 의해 세뇌당했기 때문에 천재가 아니라고 생각하게 되었다.",
//                person = "세스 고딘"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.CREATIVITY,
//                text = "영화를 미친 듯이 사랑하면 좋은 영화를 만들 수 밖에 없다.",
//                person = "쿠엔틴 타란티노"
//            )
//
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.HAPPINESS,
//                text = "음악에도 화음과 불협화음이 있지요. 불협화음 후에 들리는 화음은 더욱 아름답게 느껴져요. 불협화음이 없다며 어떻게 될까요? 화음의 아름다움을 모르게 되겠죠.",
//                person = "세이모어"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.HAPPINESS,
//                text = "웃어라, 온 세상이 너와 함께 웃을 것이다. 울어라, 너 혼자 울게 될 것이다.",
//                person = "엘라휠러 윌콕스"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.HAPPINESS,
//                text = "스스로 행복하다고 생각하지 않는 사람은 행복하지 않다.",
//                person = "퍼블릴리어스 사이러스"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.HAPPINESS,
//                text = "인생에 있어서 최고의 행복은 우리가 사랑받고 있다는 확신이다.",
//                person = "빅토르 위고"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.HAPPINESS,
//                text = "행복은 입맞춤과 같다. 행복을 얻기 위해서는 누군가에게 행복을 주어야만 한다.",
//                person = "디오도어 루빈"
//            )
//
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.CONFIDENCE,
//                text = "이 세상에 기쁜 일만 있다면 용기도 인내도 배울 수 없을 것이다.",
//                person = "헬렌 켈러"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.CONFIDENCE,
//                text = "살아남기 위해선, 후회를 안고 살아가는 법을 배워야 해",
//                person = "제이지"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.CONFIDENCE,
//                text = "(메탈리카에 대해) '우리가 걔들보다 낫다. 실력도 있다. 걔들더러 용기가 있으면 우리 바로 다음 무대에 올라 연주하라고 하고 싶다.'",
//                person = "아이언 메이든"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.CONFIDENCE,
//                text = "나는 사업가가 아니야, 내가 사업 그 자체라고, 인마",
//                person = "제이지"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.CONFIDENCE,
//                text = "언젠가 죽는다는 사실을 기억해라. 그럼 당신을 정말로 잃을게 없다.",
//                person = "스티브 잡스"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.CONFIDENCE,
//                text = "여러분 모두를 무사히 귀환시키겠다는 약속은 할 수 없다. 그러나 여러분과 전능한 하느님 앞에 이것만은 맹세한다. 우리가 전투에 투입될 때 내가 가장 먼저 전장에 앞장설 것이고 전장을 떠날 땐 내가 가장 늦게 나올 것이며, 누구도 남겨 두고 오지 않겠다. 전사했든 생존했든 우리는 모두 다 함께 고국으로 돌아올 것이다.",
//                person = "무어 중령"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.CONFIDENCE,
//                text = "살려고 하면 죽을 것이요, 죽고자 하면 살 것이다. 길목을 막으면 한 명이 능히 천명을 감당할 수 있다.",
//                person = "이순신"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.CONFIDENCE,
//                text = "우리는 전투에서 졌지만, 전쟁에는 아직 지지 않았다.",
//                person = "샤를 드골"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.CONFIDENCE,
//                text = "오랜시간동안 많은 팀들의 상승과 하락을 지켜 보았습니다. 그리고 그 끝에 있는건 항상 저였습니다.",
//                person = "페이커"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.CONFIDENCE,
//                text = "성공으로 가는 중요한 열쇠 한가지는 자신감이고, 자신감을 얻는 중요한 열쇠는 준비성이다.",
//                person = "아서 애시"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.CONFIDENCE,
//                text = "나에 대한 자신감을 잃으면 온 세상이 나의 적이 된다.",
//                person = "랄프 왈도 에머슨"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.CONFIDENCE,
//                text = "자신을 신뢰하는 사람은 타인을 부러워하지 않는다.",
//                person = "랄프 왈도 에머슨"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.CONFIDENCE,
//                text = "난 나 자신을 너무도 믿기에 그 무엇도 나를 막을 수 없다.",
//                person = "코너 맥그리거"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.HAPPINESS,
//                text = "행복에 있어서 가장 큰 장애물은 너무 큰 행복을 기대하는 마음이다.",
//                person = "넬르"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.HAPPINESS,
//                text = "모두가 행복해질 때까지는 아무도 완전히 행복해질 수는 없다.",
//                person = "H. 스펜서"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.HAPPINESS,
//                text = "그대가 행복을 추구하고 있는 한, 그대는 언제까지나 행복해지지 못한다. 그대가 소망을 버리고 이미 목표도 욕망도 없고 행복에 대해서도 말하지 않게 되었을 때 그때에야 세상의 거친 파도는 그대 마음에 미치지 않고 그대의 마음은 비로소 휴식을 안다.",
//                person = "헤르만 헤세"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.HAPPINESS,
//                text = "최고의 행복은 존재하지 않는다.",
//                person = "그라시안"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.HAPPINESS,
//                text = "행복은 불행을 피하는 것에서 성립된다.",
//                person = "앨폰스 카"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.HAPPINESS,
//                text = "행복은 훌륭한 선생이다. 하지만 역경은 그보다 더 훌륭한 선생이다.",
//                person = "윌리엄 헤즐릿"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.HAPPINESS,
//                text = "비교는 행복을 죽이는 확실한 방법이다.",
//                person = "탈무드"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.HAPPINESS,
//                text = "남을 행복하게 해 주는 것은 마치 향수를 뿌리는 일과도 같다.",
//                person = "탈무드"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.OTHER,
//                text = "젊은 날의 의무는 부패와 맞서 싸우는 것이다.",
//                person = "커트 코베인"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.OTHER,
//                text = "말을 많이 한다는 것과 잘한다는 것은 별개이다.",
//                person = "소포클레스"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.OTHER,
//                text = "남들이 나와 같지 않다는 점을 인정하라.",
//                person = "존 그레이"
//            )
//
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.WEALTH,
//                text = "부자는 끈기로 무장한 사람들이다.",
//                person = "워렌 버핏"
//            )
//
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.BUSINESS,
//                text = "당신의 가장 불행한 고객은 가장 큰 배움의 원천이다.",
//                person = "빌 게이츠"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.BUSINESS,
//                text = "고객 서비스는 특정 부서가 아니다. 회사 전체가 되어야 한다.",
//                person = "토니 쉐이"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.BUSINESS,
//                text = "경쟁에 집중하는 대신에 고객에 집중해라.",
//                person = "스캇 쿡"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.BUSINESS,
//                text = "좋은 평판을 쌓는 데에는 20년이 걸리지만 무너뜨리는 데에는 5분이 걸린다. 이 사실에 대해 생각해 보면, 당신은 일을 다르게 할 것이다.",
//                person = "워렌 버핏"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.BUSINESS,
//                text = "성공하려면 귀는 열고 입은 닫아라.",
//                person = "록 펠러"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.BUSINESS,
//                text = "임금을 지불하는 사람은 고용주가 아니다. 고용주는 돈만 관리할 뿐, 임금을 지불하는 사람은 고객이다.",
//                person = "헨리 포드"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.BUSINESS,
//                text = "보스가 좋아할 것인지 싫어할 것인지에 대해 끊임없이 걱정하는 것만큼 조직을 빨리 퇴보시키는 것은 없다.",
//                person = "도요타 기이치로"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.BUSINESS,
//                text = "서비스란 100점 아니면 0점 밖에 없으며 1점이라도 마이너스가 있으면 그것은 0점이며, 그러면 손님이 떠나버릴 가능성이 높다.",
//                person = "디즈니랜드"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.BUSINESS,
//                text = "불만을 제기한 소비자들 중 54-70%는 그들의 불만이 해결된다면, 그 기업과 다시 거래를 하게 된다. 그러나 그 불만이 신속하게 해결된다고 느끼는 경우에는 그 비율이 95%까지 상승한다.",
//                person = "필립 코틀러"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.BUSINESS,
//                text = "좋은 기업과 위대한 기업 사이에는 한 가지 차이가 있다. 좋은 기업은 훌륭한 상품과 서비스를 제공한다. 위대한 기업은 훌륭한 상품과 서비스를 제공할 뿐만 아니라, 세상을 더 나은 곳으로 만들기 위해 노력한다.",
//                person = "윌리엄 클레이 포드 주니어"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.BUSINESS,
//                text = "18년 동안 아마존을 성공으로 이끈 3가지 큰 전략이 있다. 그것은 고객을 우선으로 생각하고, 발명하고, 인내하는 것이다.",
//                person = "제프 베조스"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.BUSINESS,
//                text = "상품 진열대에서 특정 제품이 소비자의 마음을 사로잡는 시간은 평균 0.6초다. 이처럼 짧은 시간에 고객을 잡지 못하면 마케팅 싸움에서 결코 승리할 수 없다.",
//                person = "이건희"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.BUSINESS,
//                text = "경영에 가능한 한 사원을 참여시킨다. 자신도 참여하고 있다고 사원이 느끼면 일하는 태도도 달라진다.",
//                person = "고바야시 마사히로"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.BUSINESS,
//                text = "일이 즐거우면 인생은 낙원이고, 일이 의무이면 인생은 지옥이다.",
//                person = "막심 고리끼"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.BUSINESS,
//                text = "최고 경영진을 외부에서 찾는 것은 파산을 자초하는 일이다.",
//                person = "피터 드러커"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.BUSINESS,
//                text = "태도를 보고 채용하라. 스킬은 훈련으로 된다.",
//                person = "사우스웨스트 항공사"
//            )
//
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.LOVE,
//                text = "사랑하는 것은 천국을 살짝 엿보는 것이다.",
//                person = "카렌 선드"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.LOVE,
//                text = "사랑해서 사랑을 잃은 것은 전혀 사랑하지 않는 것보다 낫다.",
//                person = "알프레드 테니슨"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.LOVE,
//                text = "인생에서 가장 행복한 때는 누군가에게서 사랑받는다고 확신할 때이다.",
//                person = "빅토르 위고"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.LOVE,
//                text = "사랑받기 위해 사랑하는 것이 인간이다. 그러나 사랑하기 위하여 사랑하는 것은 천사에 가깝다.",
//                person = "A.D 라마트린"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.LOVE,
//                text = "사랑은 무엇보다도 자신을 위한 선물이다.",
//                person = "장 아누이"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.LOVE,
//                text = "진정한 사랑은 그 사람을 통해나 자신도 사랑한다.",
//                person = "칼 구츠코"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.LOVE,
//                text = "서로를 용서하는 것이야말로 가장 아름다운 사랑의 모습이다.",
//                person = "존 셰필드"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.LOVE,
//                text = "내가 이해하는 모든 것은 내가 사랑하기 때문에 이해한다.",
//                person = "레프 톨스토이"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.LOVE,
//                text = "사랑은 증오의 소음을 덮어버리는 큰 북소리다.",
//                person = "마가릿 조"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.LOVE,
//                text = "겁쟁이는 사랑을 드러낼 능력이 없다. 사랑은 용기 있는 자의 특권이다.",
//                person = "마하트마 간디"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.LOVE,
//                text = "사랑의 첫 번째 의무는 상대방에 귀 기울이는 것이다.",
//                person = "폴 틸리히"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.LOVE,
//                text = "가장 행복한 사람들은 행복을 더 많이 가지려는 자가 아니라, 더 많이 주려는 자들이다.",
//                person = "H. 잭슨 브라운 주니어"
//            )
//            quoteCreator.addNewQuote(
//                category = QuoteCategory.LOVE,
//                text = "모든 사람들이 산 정상에서 살기를 원한다. 하지만 모든 행복과 성장은 당신이 산을 오르고 있을 때 발생한다.",
//                person = "앤디 루니"
//            )
        }
}