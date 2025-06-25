package com.tianqueal.flowjet.backend.exceptions.business

import com.tianqueal.flowjet.backend.utils.constants.MessageKeys

class ProjectStatusNotFoundException : AppException {
  constructor(id: Int) : super(
    message = "Project status not found with ID: '${id}",
    errorCode = MessageKeys.ERROR_PROJECT_STATUS_NOT_FOUND,
    args = arrayOf(id)
  )
}
