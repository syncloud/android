package org.syncloud.android.core.common

import org.syncloud.android.core.common.BaseResult
import org.syncloud.android.core.common.SyncloudException

class SyncloudResultException(message: String?, var result: BaseResult) : SyncloudException(message)