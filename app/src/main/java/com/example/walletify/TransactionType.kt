package com.example.walletify

enum class TransactionType(val typeCode: Char) {
    EXPENSE('E'),
    INCOME('I'),
    SOURCE_TRANSFER('S'),
    DESTINATION_TRANSFER('D'),
    INITIAL_BALANCE('B')
}