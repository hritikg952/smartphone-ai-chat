package com.smartphoneaichat.domain.session

import com.smartphoneaichat.domain.model.AuthorizedSessionContext
import com.smartphoneaichat.domain.model.ProfileRole
import com.smartphoneaichat.domain.model.ProfileStateInvalidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Owns the selected profile and invalidates feature state during a profile transition. */
class ProfileSessionCoordinator(
    initialContext: AuthorizedSessionContext?,
    private val invalidators: List<ProfileStateInvalidator>,
) {
    private val _currentContext = MutableStateFlow(initialContext)
    val currentContext: StateFlow<AuthorizedSessionContext?> = _currentContext.asStateFlow()

    fun switchTo(context: AuthorizedSessionContext) {
        require(context.role == ProfileRole.Self) {
            "The current prototype supports switching to the self profile only."
        }
        invalidators.forEach(ProfileStateInvalidator::clearForProfileSwitch)
        _currentContext.value = context
    }

    fun clear() {
        invalidators.forEach(ProfileStateInvalidator::clearForProfileSwitch)
        _currentContext.value = null
    }
}
