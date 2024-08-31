/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.observability.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.android.observability.Injection
import com.example.android.observability.R
import com.example.android.observability.databinding.ActivityUserBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * Main screen of the app. Displays a user name and gives the option to update the user name.
 */
class UserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserBinding
    private lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: UserViewModel by viewModels { viewModelFactory }

    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModelFactory = Injection.provideViewModelFactory(this)
        binding.updateUserButton.setOnClickListener { updateUserName() }
    }

    override fun onStart() {
        super.onStart()
        // Subscribe to the emissions of the user name from the view model.
        // Update the user name text view, at every onNext emission.
        // In case of error, log the exception.
        disposable.add(
            viewModel.userName()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ this.binding.userName.text = it },
                    { error -> Log.e(TAG, "Unable to get username", error) })
        )
    }

    override fun onStop() {
        super.onStop()

        // clear all the subscription
        disposable.clear()
    }

    private fun updateUserName() {
        val userName = binding.userNameInput.text.toString()
        // Disable the update button until the user name update has been done
        binding.updateUserButton.isEnabled = false
        // Subscribe to updating the user name.
        // Enable back the button once the user name has been updated
        disposable.add(
            viewModel.updateUserName(userName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ binding.updateUserButton.isEnabled = true },
                    { error -> Log.e(TAG, "Unable to update username", error) })
        )
    }

    companion object {
        private val TAG = UserActivity::class.java.simpleName
    }
}
