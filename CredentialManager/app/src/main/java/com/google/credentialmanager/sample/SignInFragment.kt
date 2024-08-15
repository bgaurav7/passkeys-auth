/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.credentialmanager.sample

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.GetPasswordOption
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PasswordCredential
import androidx.credentials.PendingGetCredentialRequest
import androidx.credentials.PublicKeyCredential
import androidx.credentials.pendingGetCredentialRequest
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.credentialmanager.sample.databinding.FragmentSignInBinding
import com.google.credentialmanager.sample.network.APIClient
import com.google.credentialmanager.sample.network.AuthModel
import com.google.credentialmanager.sample.utils.Signature
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignInFragment : Fragment() {

    private val TAG = "SignIn"
    private lateinit var credentialManager: CredentialManager
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!
    private lateinit var listener: SignInFragmentCallback

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as SignInFragmentCallback
        } catch (castException: ClassCastException) {
            /** The activity does not implement the listener.  */
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        credentialManager = CredentialManager.create(requireActivity())

        binding.signInWithSavedCredentials.setOnClickListener(
            signInWithSavedCredentials()
        )
    }

    private fun configureGetCredentialRequest(responseJson: String): GetCredentialRequest {
        val getPublicKeyCredentialOption =
            GetPublicKeyCredentialOption(responseJson, null)
        val getPasswordOption = GetPasswordOption()
        val getCredentialRequest = GetCredentialRequest(
            listOf(
                getPublicKeyCredentialOption,
                getPasswordOption
            )
        )
        return getCredentialRequest
    }

    private fun signInWithSavedCredentials(): View.OnClickListener {
        return View.OnClickListener {
            if (binding.textUsername.text.isNullOrEmpty()) {
                binding.textUsername.error = "User name required"
                binding.textUsername.requestFocus()
            } else {
                val username = binding.textUsername.text.toString();
                Log.i(TAG, "Username: $username")
                val call = APIClient.apiService.loginStart(AuthModel(username, ""))

                call.enqueue(object : Callback<JsonObject> {
                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                        val loginReq = response.body().toString()
                        Log.i(TAG, "Login Req: $loginReq")

                        lifecycleScope.launch {
                            configureViews(View.VISIBLE, false)

                            val getCredentialRequest = configureGetCredentialRequest(loginReq)

                            val data = getSavedCredentials(getCredentialRequest)

                            data?.let {
                                if (data.credential is PublicKeyCredential) {
                                    val cred = data.credential as PublicKeyCredential
                                    DataProvider.setSignedInThroughPasskeys(true)

                                    Log.d(TAG, "Passkey: ${cred.authenticationResponseJson}")

                                    showHomeWithPasskeys(username, cred.authenticationResponseJson)
                                }
                                if (data.credential is PasswordCredential) {
                                    val cred = data.credential as PasswordCredential
                                    DataProvider.setSignedInThroughPasskeys(false)
                                    Log.d(TAG,"Got Password - User:${cred.id} Password: ${cred.password}")

                                    showHomeWithPassword(cred.id, cred.password)
                                }
                                if (data.credential is CustomCredential) {
                                    //If you are also using any external sign-in libraries, parse them here with the
                                    // utility functions provided.
                                }

                                configureViews(View.INVISIBLE, true)
                            }
                        }
                    }

                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        Log.e(TAG, "Error : " + t.message)
                        val message = "Unable to connect to server. Error: " + t.message
                        activity?.showErrorAlert(message)
                    }
                })
            }
        }
    }

    private fun showHomeWithPasskeys(username: String, data: String) {
        val callEnd = APIClient.apiService.loginFinish(AuthModel(username, data))

        Log.i(TAG, "Login Authentication Finish: Req $data")

        callEnd.enqueue(object : Callback<JsonObject> {
            override fun onResponse(
                call: Call<JsonObject>,
                response: Response<JsonObject>
            ) {
                Log.i(TAG, "Login Authentication Finish: " + response.code() + " " + response.message())

                if(response.code() == 200) {
                    DataProvider.setSignedInThroughPasskeys(true)
                    listener.showHome()
                } else {
                    val message = "Unable to login using credentials"
                    activity?.showErrorAlert(message)
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.e(TAG, "Error : " + t.message)
                val message = "Unable to connect to server for authentication. Error: " + t.message
                activity?.showErrorAlert(message)
            }
        })
    }

    private fun showHomeWithPassword(username: String, password: String) {
        DataProvider.setSignedInThroughPasskeys(false)
        listener.showHome()
    }

    private fun configureViews(visibility: Int, flag: Boolean) {
        configureProgress(visibility)
        binding.signInWithSavedCredentials.isEnabled = flag
    }

    private fun configureProgress(visibility: Int) {
        binding.textProgress.visibility = visibility
        binding.circularProgressIndicator.visibility = visibility
    }

//    private fun fetchAuthJsonFromServer(): String {
//        return requireContext().readFromAsset("AuthFromServer")
//    }

//    private fun sendSignInResponseToServer(): Boolean {
//        return true
//    }

    private suspend fun getSavedCredentials(getCredentialRequest: GetCredentialRequest): GetCredentialResponse? {

        val result = try {
            credentialManager.getCredential(
                requireActivity(),
                getCredentialRequest,
            )
        } catch (e: Exception) {
//            configureViews(View.INVISIBLE, true)
            Log.e(TAG, "getCredential failed with exception: " + e.message.toString())
            activity?.showErrorAlert(
                "An error occurred while authenticating through saved credentials. Check logs for additional details"
            )
            return null
        }

        return result
    }

    override fun onDestroyView() {
        super.onDestroyView()
        configureProgress(View.INVISIBLE)
        _binding = null
    }

    interface SignInFragmentCallback {
        fun showHome()
    }
}
