package com.example.android.chessclock

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.util.Log
import android.view.View
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_themes.*

const val START_TIME = 600 //change top/bot
const val INCREMENT = 5 //change top/bot

class MainActivity : AppCompatActivity() {

    enum class ClockStates { CLOCK_START, CLOCK_END }

    private lateinit var countDownTimerBot: CountDownTimer
    private lateinit var countDownTimerTop: CountDownTimer

    var clockState: ClockStates = ClockStates.CLOCK_START
    var time_in_seconds_bot = START_TIME
    var time_in_seconds_top = START_TIME
    private var isNightModeOn: Boolean = false

    private var gameActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_main)
        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val sharedPrefsEdit: SharedPreferences.Editor = sharedPreferences.edit()

        /**
         * Set up new session
         */
        loadData()
        greyOutButtons()

        /**
         * Button listeners
         */
        pause_button.setOnClickListener {
            pauseState()
        }
        restart_button.setOnClickListener {
            restartGame()
        }
        settings_button.setOnClickListener {
            openSettings()
        }

        /**
         * If game is active start clock from opponent, else start own clock
         */
        top_sq.setOnClickListener {
            if (gameActive) {
                when (clockState) {
                    ClockStates.CLOCK_START -> startTimerBot(time_in_seconds_bot) //add increments hier nog
                    ClockStates.CLOCK_END -> resetTimers()
                }
            } else {
                when (clockState) {
                    ClockStates.CLOCK_START -> startTimerTop(time_in_seconds_top) //add increments hier nog
                    ClockStates.CLOCK_END -> resetTimers()
                }
            }
        }

        /**
         * If game is active start clock from opponent, else start own clock
         */
        bot_sq.setOnClickListener {
            if (gameActive) {
                when (clockState) {
                    ClockStates.CLOCK_START -> startTimerTop(time_in_seconds_top) //add increments hier nog
                    ClockStates.CLOCK_END -> resetTimers()
                }
            } else {
                when (clockState) {
                    ClockStates.CLOCK_START -> startTimerBot(time_in_seconds_bot) //add increments hier nog
                    ClockStates.CLOCK_END -> resetTimers()
                }
            }
        }
    }

    /**
     * Show dialog to restart game
     */
    private fun restartGame() {
        AlertDialog.Builder(this)
            .setMessage("Restart?")
            .setPositiveButton(getString(R.string.yes)) { _, _ -> // dialog, whichButton are never used
                resetTimers()
                greyOutButtons()
                top_sq.setBackgroundColor(getColor(R.color.colorPrimaryDark))
                bot_sq.setBackgroundColor(getColor(R.color.colorPrimaryDark))
                gameActive = false
                top_sq.isClickable = true
                bot_sq.isClickable = true
            }
            .setNegativeButton(getString(R.string.no)) { _, _ -> // dialog, whichButton are never used
                // Closes dialog
            }
            .show()
    }

    /**
     * Grey out restart and pause buttons
     */
    private fun greyOutButtons() {
        restart_button.alpha = .5f
        restart_button.isClickable = false
        pause_button.alpha = .5f
        pause_button.isClickable = false
    }

    /**
     * Pause game and set up for game to start with whichever player is preferred to start again
     */
    private fun pauseState() {
        pauseTimerTop()
        pauseTimerBot()
        top_sq.isClickable = true
        bot_sq.isClickable = true
        top_sq.setBackgroundColor(getColor(R.color.colorPrimaryDark))
        bot_sq.setBackgroundColor(getColor(R.color.colorPrimaryDark))
        gameActive = false
        pause_button.alpha = .5f
        pause_button.isClickable = false
    }

    /**
     * Set timers to new values ???
     */
    private fun resetTimers() {
        //add pop-up Are You Sure? [YES/NO]
        pauseTimerTop()
        pauseTimerBot()
        time_in_seconds_bot = START_TIME
        time_in_seconds_top = START_TIME
        updateTextUIBot()
        updateTextUITop()
        clockState = ClockStates.CLOCK_START
    }

    /**
     * Pause timer and light up opponents screen
     */
    private fun pauseTimerBot() {
        if (this::countDownTimerBot.isInitialized) {
            countDownTimerBot.cancel()
            // Switch primary colors when turn ends
            bot_sq.setBackgroundColor(getColor(R.color.colorPrimaryDark))
            top_sq.setBackgroundColor(getColor(R.color.colorPrimary))
        }
    }

    private fun pauseTimerTop() {
        if (this::countDownTimerTop.isInitialized) {
            countDownTimerTop.cancel()
            // Switch primary colors when turn ends
            top_sq.setBackgroundColor(getColor(R.color.colorPrimaryDark))
            bot_sq.setBackgroundColor(getColor(R.color.colorPrimary))
        }
    }

    /**
     * If game hasn't started light up player clock when starting it and make buttons active,
     * else just start timer
     */
    private fun startTimerBot(seconds: Int) {
        if (gameActive) {
            countDownTimerBot = object : CountDownTimer(seconds * 1000L, 1000) {
                override fun onFinish() {
                    Log.d("T", "Bottom timer has ended!")
                    clockState = ClockStates.CLOCK_END
                }

                override fun onTick(p0: Long) {
                    time_in_seconds_bot = (p0 / 1000L).toInt()
                    updateTextUIBot()
                }
            }
            countDownTimerBot.start()

            top_sq.isClickable = false
            bot_sq.isClickable = true
            pauseTimerTop()
            clockState = ClockStates.CLOCK_START
        } else {
            bot_sq.setBackgroundColor(getColor(R.color.colorPrimary))

            gameActive = true

            countDownTimerBot = object : CountDownTimer(seconds * 1000L, 1000) {
                override fun onFinish() {
                    Log.d("T", "Bottom timer has ended!")
                    clockState = ClockStates.CLOCK_END
                }

                override fun onTick(p0: Long) {
                    time_in_seconds_bot = (p0 / 1000L).toInt()
                    updateTextUIBot()
                }
            }
            countDownTimerBot.start()

            makeButtonsActive()

            top_sq.isClickable = false
            bot_sq.isClickable = true
            pauseTimerTop()
            clockState = ClockStates.CLOCK_START
        }
    }

    /**
     * If game hasn't started light up player clock when starting it and make buttons active,
     * else just start timer
     */

    private fun startTimerTop(seconds: Int) {
        if (gameActive) {
            countDownTimerTop = object : CountDownTimer(seconds * 1000L, 1000) {
                override fun onFinish() {
                    Log.d("T", "Top timer has ended!")
                    clockState = ClockStates.CLOCK_END
                }

                override fun onTick(p0: Long) {
                    time_in_seconds_top = (p0 / 1000L).toInt()
                    updateTextUITop()
                }
            }
            countDownTimerTop.start()

            top_sq.isClickable = true
            bot_sq.isClickable = false
            pauseTimerBot()
            clockState = ClockStates.CLOCK_START
        } else {
            top_sq.setBackgroundColor(getColor(R.color.colorPrimary))

            gameActive = true

            countDownTimerTop = object : CountDownTimer(seconds * 1000L, 1000) {
                override fun onFinish() {
                    Log.d("T", "Top timer has ended!")
                    clockState = ClockStates.CLOCK_END
                }

                override fun onTick(p0: Long) {
                    time_in_seconds_top = (p0 / 1000L).toInt()
                    updateTextUITop()
                }
            }
            countDownTimerTop.start()

            makeButtonsActive()

            top_sq.isClickable = true
            bot_sq.isClickable = false
            pauseTimerBot()
            clockState = ClockStates.CLOCK_START
        }
    }

    /**
     * Make buttons active
     */
    private fun makeButtonsActive() {
        restart_button.alpha = 1f
        restart_button.isClickable = true
        pause_button.alpha = 1f
        pause_button.isClickable = true
    }

    /**
     * Open Settings menu
     */
    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    /**
     * Update timers
     */
    private fun updateTextUIBot() {
        bot_clock.text = Clock(time_in_seconds_bot).updateText()
    }

    private fun updateTextUITop() {
        top_clock.text = Clock(time_in_seconds_top).updateText()
    }

    /**
     * Load sharedPreferences
     */
    private fun loadData() {
        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        isNightModeOn = sharedPreferences.getBoolean("BOOLEAN_KEY", false)

        if (isNightModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    /**
     * Show dialog to close when 'Android back button' pressed
     */
    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setMessage("Close?")
            .setPositiveButton(android.R.string.ok) { _, _ -> // dialog, whichButton are never used
                super.onBackPressed()
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> // dialog, whichButton are never used
                // Closes dialog
            }
            .show()
    }
}

