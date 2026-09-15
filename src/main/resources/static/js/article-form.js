(() => {
    const form = document.querySelector('#article-form');
    if (!form) return;
    const saveButton = document.querySelector('#save-post');
    const publishButton = document.querySelector('#publish-post');
    const deleteButton = document.querySelector('#delete-post');
    const messageBox = document.querySelector('#form-message');
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
    const mode = form.dataset.mode;
    const postId = form.dataset.postId;
    const messages = form.dataset;

    function showMessage(message, success) {
        messageBox.textContent = message;
        messageBox.classList.remove('d-none', 'alert-success', 'alert-danger');
        messageBox.classList.add(success ? 'alert-success' : 'alert-danger');
        messageBox.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }

    async function apiRequest(url, options) {
        const headers = { ...(options.headers || {}) };
        if (csrfHeader && csrfToken) headers[csrfHeader] = csrfToken;
        const response = await fetch(url, { ...options, headers });
        if (!response.ok) {
            const message = (await response.text()).trim();
            throw new Error(message || messages.requestFailed.replace('__STATUS__', response.status));
        }
        return response;
    }

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        form.classList.add('was-validated');
        if (!form.checkValidity()) return;
        const intent = event.submitter?.dataset.intent || 'save';
        if (intent === 'publish' && !window.confirm(messages.publishQuestion)) {
            return;
        }
        const activeButton = event.submitter || saveButton;
        const originalText = activeButton.textContent;
        saveButton.disabled = true;
        if (publishButton) publishButton.disabled = true;
        activeButton.textContent = intent === 'publish' ? messages.publishing : messages.saving;
        const body = JSON.stringify({
            title: document.querySelector('#title').value.trim(),
            content: document.querySelector('#content').value.trim()
        });
        try {
            const creating = mode === 'create';
            const response = await apiRequest(creating ? '/api/admin/articles' : `/api/admin/articles/${postId}`, {
                method: creating ? 'POST' : 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body
            });
            if (creating) {
                const created = await response.json();
                if (intent === 'publish') {
                    await apiRequest(`/api/admin/articles/${created.id}/publish`, { method: 'PUT' });
                }
                window.location.assign('/admin');
            } else {
                showMessage(messages.saved, true);
                saveButton.disabled = false;
                activeButton.textContent = originalText;
            }
        } catch (error) {
            showMessage(error.message, false);
            saveButton.disabled = false;
            if (publishButton) publishButton.disabled = false;
            activeButton.textContent = originalText;
        }
    });

    deleteButton?.addEventListener('click', async () => {
        const title = deleteButton.dataset.postTitle;
        if (!window.confirm(messages.deleteQuestion.replace('__TITLE__', title))) return;
        deleteButton.disabled = true;
        deleteButton.textContent = messages.deleting;
        try {
            await apiRequest(`/api/admin/articles/${postId}`, { method: 'DELETE' });
            window.location.assign('/admin');
        } catch (error) {
            showMessage(error.message, false);
            deleteButton.disabled = false;
            deleteButton.textContent = deleteButton.dataset.defaultLabel;
        }
    });
})();
