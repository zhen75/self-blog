(() => {
    const form = document.querySelector('#login-form');
    if (!form) return;
    const submitButton = document.querySelector('#login-submit');
    const passwordInput = document.querySelector('#password');
    const toggleButton = document.querySelector('#toggle-password');
    const eyeOpen = document.querySelector('#eye-open');
    const eyeClosed = document.querySelector('#eye-closed');
    const messageBox = document.querySelector('#login-message');
    const messages = form.dataset;

    toggleButton?.addEventListener('click', () => {
        const showing = passwordInput.type === 'text';
        passwordInput.type = showing ? 'password' : 'text';
        eyeOpen.classList.toggle('d-none', !showing);
        eyeClosed.classList.toggle('d-none', showing);
        toggleButton.setAttribute('aria-label', showing ? messages.showPassword : messages.hidePassword);
        toggleButton.setAttribute('aria-pressed', String(!showing));
    });

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        form.classList.add('was-validated');
        if (!form.checkValidity()) {
            return;
        }

        messageBox.classList.add('d-none');
        submitButton.disabled = true;
        submitButton.textContent = messages.submitting;

        try {
            const response = await fetch(form.action, {
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: new URLSearchParams(new FormData(form))
            });
            const result = await response.json();
            if (!response.ok) {
                throw new Error(result.message || messages.genericError);
            }
            window.location.assign(result.redirect || '/admin');
        } catch (error) {
            messageBox.textContent = error.message || messages.genericError;
            messageBox.classList.remove('d-none');
            submitButton.disabled = false;
            submitButton.textContent = messages.submitLabel;
            passwordInput.focus();
            passwordInput.select();
        }
    });
})();
