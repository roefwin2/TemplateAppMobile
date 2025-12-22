package org.society.appname.core.usecases

import kotlinx.coroutines.test.runTest
import org.society.appname.authentication.AuthResult
import org.society.appname.testing.FakeAuthRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SaveUserDataUseCaseTest {

    @Test
    fun returnsSuccessWithoutCallingRepositoryWhenDataIsEmpty() = runTest {
        val fakeRepository = FakeAuthRepository()
        val useCase = SaveUserDataUseCase(fakeRepository)

        val result = useCase(emptyMap())

        assertTrue(result is AuthResult.Success)
        assertEquals(0, fakeRepository.saveUserDataCalls)
    }
}
