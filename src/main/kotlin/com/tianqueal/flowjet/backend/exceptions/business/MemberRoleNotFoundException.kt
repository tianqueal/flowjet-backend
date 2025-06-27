package com.tianqueal.flowjet.backend.exceptions.business

import com.tianqueal.flowjet.backend.utils.constants.MessageKeys
import com.tianqueal.flowjet.backend.utils.enums.MemberRoleEnum

class MemberRoleNotFoundException : AppException {
    constructor(id: Int) : super(
        message = "Member role not found with ID: '$id'",
        errorCode = MessageKeys.ERROR_MEMBER_ROLE_NOT_FOUND,
    )

    constructor(code: MemberRoleEnum) : super(
        message = "Member role not found with code: '$code'",
        errorCode = MessageKeys.ERROR_MEMBER_ROLE_NOT_FOUND,
    )
}
