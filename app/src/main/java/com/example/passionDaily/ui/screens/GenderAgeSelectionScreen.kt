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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.passionDaily.ui.viewmodels.SharedSignInViewModel
import com.example.passionDaily.util.AgeGroup
import com.example.passionDaily.util.Gender

@Composable
fun GenderAgeSelectionScreen(
    userProfileJsonV2: String? = null,
    sharedSignInViewModel: SharedSignInViewModel = hiltViewModel(),
) {
    val selectedGender by sharedSignInViewModel.selectedGender.collectAsState()
    val selectedAgeGroup by sharedSignInViewModel.selectedAgeGroup.collectAsState()

    val isNextEnabled = sharedSignInViewModel.isNextEnabled()


    GenderAgeSelectionScreenContent(
        selectedGender = selectedGender,
        selectedAgeGroup = selectedAgeGroup,
        isNextEnabled = isNextEnabled,

        onGenderSelected = { gender ->
            sharedSignInViewModel.selectGender(gender)
        },
        onAgeGroupSelected = { ageGroup ->
            sharedSignInViewModel.selectAgeGroup(ageGroup)
        },
        onNextClicked = {
            sharedSignInViewModel.handleNextClicked(userProfileJsonV2)
        }
    )
}

@Composable
fun GenderAgeSelectionScreenContent(
    selectedGender: Gender?,
    selectedAgeGroup: AgeGroup?,
    isNextEnabled: Boolean,
    onGenderSelected: (Gender?) -> Unit,
    onAgeGroupSelected: (AgeGroup?) -> Unit,
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
                    isNextEnabled = isNextEnabled,
                    onNextClicked = { onNextClicked() }
                )
            }
        }
    }
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
    isNextEnabled: Boolean,
    onNextClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(345.dp)
            .height(57.dp)
            .background(
                color = if (isNextEnabled) Color.White else Color(0xFF737373),
                shape = RoundedCornerShape(size = 4.dp)
            )
            .clickable(enabled = isNextEnabled) { onNextClicked() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "다음",
            style = TextStyle(
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
                color = if (isNextEnabled) Color.Black else Color(0xFFABABAB)
            )
        )
    }
}