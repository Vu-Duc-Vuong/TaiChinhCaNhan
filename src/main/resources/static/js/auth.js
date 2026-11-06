document.querySelectorAll(".password-toggle").forEach((button) => {
          button.addEventListener("click", () => {
                    const input = document.getElementById(button.dataset.target);
                    const isPassword = input.type === "password";
                    input.type = isPassword ? "text" : "password";
                    button.textContent = isPassword ? "Ẩn" : "Hiện";
          });
});