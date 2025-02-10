package com.example.passionDaily.login.action

import com.example.passionDaily.login.base.SharedLogInActions

interface ConsentActions : SharedLogInActions {
    override fun toggleAgreeAll()
    override fun toggleIndividualItem(item: String)
    override fun handleNextClick(userProfileJson: String?)
}