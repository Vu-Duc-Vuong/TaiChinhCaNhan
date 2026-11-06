package com.example.taichinhcanhan.entity;

public enum TransactionType {
          INCOME("Thu nhập"),
          EXPENSE("Chi tiêu");

          private final String displayName;

          TransactionType(String displayName) {
                    this.displayName = displayName;
          }

          public String getDisplayName() {
                    return displayName;
          }
}