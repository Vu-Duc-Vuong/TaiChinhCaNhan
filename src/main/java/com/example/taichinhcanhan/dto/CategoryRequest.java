package com.example.taichinhcanhan.dto;

import com.example.taichinhcanhan.entity.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CategoryRequest {

          @NotBlank(message = "Vui lòng nhập tên danh mục.")
          @Size(max = 100, message = "Tên danh mục không được vượt quá 100 ký tự.")
          private String name;

          @NotNull(message = "Vui lòng chọn loại danh mục.")
          private CategoryType type;

          @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự.")
          private String description;

          public String getName() {
                    return name;
          }

          public void setName(String name) {
                    this.name = name;
          }

          public CategoryType getType() {
                    return type;
          }

          public void setType(CategoryType type) {
                    this.type = type;
          }

          public String getDescription() {
                    return description;
          }

          public void setDescription(String description) {
                    this.description = description;
          }
}