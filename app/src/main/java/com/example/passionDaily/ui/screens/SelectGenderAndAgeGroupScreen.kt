package com.example.passionDaily.ui.screens

import android.widget.Toast
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passionDaily.R
import com.example.passionDaily.ui.theme.BlackBackground

@Composable
fun SelectGenderAndAgeGroupScreen() {
    SelectGenderAndAgeGroupScreenContent()
}

@Composable
fun SelectGenderAndAgeGroupScreenContent() {
    var selectedAgeGroup by remember { mutableStateOf<String?>(null) }
    var selectedGender by remember { mutableStateOf<String?>(null) }

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
                SkipButton()
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 34.dp, vertical = 24.dp)
                    .weight(1f)
            ) {
                HeaderSection()

                Spacer(modifier = Modifier.height(32.dp))

                GenderSelectionContent { gender ->
                    selectedGender = gender
                }

                Spacer(modifier = Modifier.height(15.dp))

                AgeGroupSelectionContent { selectedGroup ->
                    selectedAgeGroup = selectedGroup
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                NextActionButton(
                    selectedAgeGroup = selectedAgeGroup,
                    selectedGender = selectedGender
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
        )
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
fun GenderSelectionContent(onGenderSelected: (String?) -> Unit) {
    var selectedGender by remember { mutableStateOf<String?>(null) }

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
                    isSelected = selectedGender == "여자",
                    onClick = {
                        selectedGender = if (selectedGender == "여자") null else "여자"
                        onGenderSelected(selectedGender)
                    }
                )

                GenderOption(
                    iconId = R.drawable.male_icon,
                    selectedIconId = R.drawable.male_icon_selected,
                    description = "남자",
                    isSelected = selectedGender == "남자",
                    onClick = {
                        selectedGender = if (selectedGender == "남자") null else "남자"
                        onGenderSelected(selectedGender)
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
fun AgeGroupSelectionContent(onAgeGroupSelected: (String?) -> Unit) {
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
        AgeGroupOptions(onAgeGroupSelected = onAgeGroupSelected)
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
fun AgeGroupOptions(onAgeGroupSelected: (String?) -> Unit) {
    val ageGroups = listOf("10대", "20대", "30대", "40대", "50대", "기타")
    var selectedAgeGroup by remember { mutableStateOf<String?>(null) }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ageGroups.chunked(3).forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowItems.forEach { ageGroup ->
                    AgeGroupBox(
                        text = ageGroup,
                        isSelected = selectedAgeGroup == ageGroup,
                        onSelect = {
                            // 현재 선택된 연령대와 같은 버튼을 다시 누르면 선택 해제
                            selectedAgeGroup = if (selectedAgeGroup == ageGroup) null else ageGroup
                            onAgeGroupSelected(selectedAgeGroup) // 선택 상태 전달
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
    selectedAgeGroup: String?,
    selectedGender: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .width(345.dp)
            .height(57.dp)
            .background(color = Color.White, shape = RoundedCornerShape(size = 4.dp))
            .clickable {
                when {
                    selectedGender == null -> {
                        Toast
                            .makeText(
                                context,
                                "성별을 선택해주세요.",
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    }

                    selectedAgeGroup == null -> {
                        Toast
                            .makeText(
                                context,
                                "연령대를 선택해주세요.",
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    }

                    else -> {
                        // Proceed to next screen or action
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "다음",
            style = TextStyle(
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
                color = Color.Black
            )
        )
    }
}


@Preview(showBackground = true)
@Composable
fun SelectGenderAndAgeGroupScreenContentPreview() {
    SelectGenderAndAgeGroupScreenContent()
}
