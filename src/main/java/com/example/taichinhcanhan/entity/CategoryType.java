package com.example.taichinhcanhan.entity;

public enum CategoryType {
          INCOME("Thu nhập"),
          EXPENSE("Chi tiêu");

          private final String displayName;

          CategoryType(String displayName) {
                    this.displayName = displayName;
          }

          public String getDisplayName() {
                    return displayName;
          }
}