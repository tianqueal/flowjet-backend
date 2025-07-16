package com.tianqueal.flowjet.backend.exceptions.business

import com.tianqueal.flowjet.backend.utils.constants.CommentConstants
import com.tianqueal.flowjet.backend.utils.constants.MessageKeys

class CommentNestingLimitExceededException : AppException {
    constructor(commentId: Long?) : super(
        message = "Comment nesting limit exceeded for Comment ID: '$commentId' at nesting level: '${CommentConstants.MAX_COMMENT_DEPTH}'",
        errorCode = MessageKeys.ERROR_COMMENT_NESTING_LIMIT_EXCEEDED,
    )
}
