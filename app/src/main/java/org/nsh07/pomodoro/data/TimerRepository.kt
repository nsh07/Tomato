package org.nsh07.pomodoro.data

interface TimerRepository {
    var focusTime: Int
    var shortBreakTime: Int
    var longBreakTime: Int
    var sessionLength: Int
}

class AppTimerRepository : TimerRepository {
    override var focusTime = 25 * 60 * 1000
    override var shortBreakTime = 5 * 60 * 1000
    override var longBreakTime = 15 * 60 * 1000
    override var sessionLength = 4
}