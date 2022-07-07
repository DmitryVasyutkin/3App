package io.mobidoo.data.utils

import io.mobidoo.domain.common.ResultData
import retrofit2.Response

object TransformUtils {

    fun <T> Response<T>.toResultData() : ResultData<T> {
        return when(this.code()){
            in 200..300 -> {
                if (body() != null)
                    return ResultData.Success(body()!!)
                else ResultData.Failure("null body")
            }
            else -> {
                return ResultData.Failure(code().toString())
            }
        }
    }

    fun <T, R> transformResult(resultData: ResultData<out T>, transform: (T) -> (R)) : ResultData<R> {
        return when(resultData){
            is ResultData.Success -> {
                ResultData.Success(transform(resultData.value))
            }
            is ResultData.Loading -> {
                ResultData.Loading()
            }
            is ResultData.Failure -> {
                ResultData.Failure(resultData.message)
            }
        }

    }
}