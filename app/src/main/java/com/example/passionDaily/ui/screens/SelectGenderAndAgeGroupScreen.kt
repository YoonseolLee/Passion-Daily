package com.example.passionDaily.ui.screens

import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.passionDaily.R
import com.example.passionDaily.ui.theme.BlackBackground
import com.example.passionDaily.ui.viewmodels.SelectGenderAndAgeGroupScreenViewModel
import com.example.passionDaily.util.AgeGroup
import com.example.passionDaily.util.Gender
import kotlinx.coroutines.launch

@Composable
fun SelectGenderAndAgeGroupScreen(
    pendingUserMap: Map<String, Any>?,
    onSkip: () -> Unit,
    onNextClicked: () -> Unit,
    viewModel: SelectGenderAndAgeGroupScreenViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()

    var selectedGender by remember { mutableStateOf<Gender?>(null) }
    var selectedAgeGroup by remember { mutableStateOf<AgeGroup?>(null) }

    SelectGenderAndAgeGroupScreenContent(
        selectedGender = selectedGender,
        selectedAgeGroup = selectedAgeGroup,

        onGenderSelected = { gender ->
            selectedGender = gender
        },
        onAgeGroupSelected = { ageGroup ->
            selectedAgeGroup = ageGroup
        },

        onSkip = {
            coroutineScope.launch {
                viewModel.completeUserRegistration(
                    pendingUserMap = pendingUserMap,
                    isSkipped = true
                )
                onSkip()
            }
        },

        onNextClicked = {
            coroutineScope.launch {
                viewModel.completeUserRegistration(
                    pendingUserMap = pendingUserMap,
                    gender = selectedGender,
                    ageGroup = selectedAgeGroup
                )
                onNextClicked()
            }
        }
    )
}


@Composable
fun SelectGenderAndAgeGroupScreenContent(
    selectedGender: Gender?,
    selectedAgeGroup: AgeGroup?,
    onGenderSelected: (Gender?) -> Unit,
    onAgeGroupSelected: (AgeGroup?) -> Unit,
    onSkip: () -> Unit,
    onNextClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, end = 34.dp),
                horizontalArrangement = Arrangement.End
            ) {
                SkipButton(
                    modifier = Modifier.clickable { onSkip() }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 34.dp, vertical = 24.dp)
                    .weight(1f)
            ) {
                HeaderSection()

                Spacer(modifier = Modifier.height(32.dp))

                GenderSelectionContent(
                    selectedGender = selectedGender,
                    onGenderSelected = onGenderSelected
                )

                Spacer(modifier = Modifier.height(15.dp))

                AgeGroupSelectionContent(
                    selectedAgeGroup = selectedAgeGroup,
                    onAgeGroupSelected = onAgeGroupSelected
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                NextActionButton(
                    selectedAgeGroup = selectedAgeGroup,
                    selectedGender = selectedGender,
                    onNextClicked = { onNextClicked() }
                )
            }
        }
    }
}


@Composable
fun SkipButton(
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(id = R.string.skip_text),
        style = TextStyle(
            fontSize = 20.sp,
            fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
            color = Color.White
        ),
        modifier = Modifier
    )
}

@Composable
fun HeaderSection() {
    Column {
        Text(
            text = stringResource(id = R.string.header_title_select_screen),
            style = TextStyle(
                fontSize = 32.sp,
                fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        )
        Text(
            text = stringResource(R.string.header_subtitle_select_screen),
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
                color = Color(0xFFABABAB)
            )
        )
    }
}

@Composable
fun GenderSelectionContent(
    selectedGender: Gender?,
    onGenderSelected: (Gender?) -> Unit
) {

    Column {
        Text(
            text = "성별",
            style = TextStyle(
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
                color = Color(0xFFE8E8E8)
            )
        )

        Spacer(modifier = Modifier.height(7.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(50.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GenderOption(
                    iconId = R.drawable.female_icon,
                    selectedIconId = R.drawable.female_icon_selected,
                    description = "여자",
                    isSelected = selectedGender == Gender.F,
                    onClick = {
                        onGenderSelected(if (selectedGender == Gender.F) null else Gender.F)
                    }
                )

                GenderOption(
                    iconId = R.drawable.male_icon,
                    selectedIconId = R.drawable.male_icon_selected,
                    description = "남자",
                    isSelected = selectedGender == Gender.M,
                    onClick = {
                        onGenderSelected(if (selectedGender == Gender.M) null else Gender.M)
                    }
                )
            }
        }
    }
}

@Composable
fun GenderOption(
    iconId: Int,
    selectedIconId: Int,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(16.dp)
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(id = if (isSelected) selectedIconId else iconId),
            contentDescription = description,
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = TextStyle(
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
                color = if (isSelected) Color.White else Color(0xFF737373)
            )
        )
    }
}

@Composable
fun AgeGroupSelectionContent(
    selectedAgeGroup: AgeGroup?,
    onAgeGroupSelected: (AgeGroup?) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "연령대",
            style = TextStyle(
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
                color = Color(0xFFE8E8E8)
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        AgeGroupOptions(
            selectedAgeGroup = selectedAgeGroup,
            onAgeGroupSelected = onAgeGroupSelected
        )
    }
}

@Composable
fun AgeGroupBox(
    text: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = if (isSelected) Color.White else Color.Black,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onSelect() }
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFF1A3C96) else Color(0xFF737373),
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
                color = if (isSelected) Color(0xFF1A3C96) else Color(0xFF737373)
            )
        )
    }
}

@Composable
fun AgeGroupOptions(
    selectedAgeGroup: AgeGroup?,
    onAgeGroupSelected: (AgeGroup?) -> Unit
) {
    val ageGroups = listOf(
        AgeGroup.TEENS to "10대",
        AgeGroup.TWENTIES to "20대",
        AgeGroup.THIRTIES to "30대",
        AgeGroup.FORTIES to "40대",
        AgeGroup.FIFTIES to "50대",
        AgeGroup.ETC to "기타"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ageGroups.chunked(3).forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowItems.forEach { (ageGroupEnum, ageGroupText) ->
                    AgeGroupBox(
                        text = ageGroupText,
                        isSelected = selectedAgeGroup == ageGroupEnum,
                        onSelect = {
                            val newAgeGroup =
                                if (selectedAgeGroup == ageGroupEnum) null else ageGroupEnum
                            onAgeGroupSelected(newAgeGroup)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                    )
                }

                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun NextActionButton(
    selectedAgeGroup: AgeGroup?,
    selectedGender: Gender?,
    onNextClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEnabled = selectedGender != null && selectedAgeGroup != null

    Box(
        modifier = modifier
            .width(345.dp)
            .height(57.dp)
            .background(
                color = if (isEnabled) Color.White else Color(0xFF737373),
                shape = RoundedCornerShape(size = 4.dp)
            )
            .clickable(enabled = isEnabled) { onNextClicked() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "다음",
            style = TextStyle(
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
                color = if (isEnabled) Color.Black else Color(0xFFABABAB)
            )
        )
    }
}

//@Preview(showBackground = true)
//@Composable
//fun SelectGenderAndAgeGroupScreenContentPreview() {
//    SelectGenderAndAgeGroupScreenContent()
//}
