package com.example.taichinhcanhan.entity;

public enum MoneySourceType {
          CASH("Tiền mặt"),
          BANK("Tài khoản ngân hàng"),
          E_WALLET("Ví điện tử"),
          OTHER("Khác");

          private final String displayName;

          MoneySourceType(String displayName) {
                    this.displayName = displayName;
          }

          public String getDisplayName() {
                    return displayName;
          }
}