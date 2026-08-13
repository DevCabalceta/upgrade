$(function () {
    const $page = $('#page-content');
    const THEME_KEY = 'upgrade-theme';
    const validTabs = ['perfil', 'seguridad', 'apariencia', 'sesiones'];

    function showTab(tab) {
        if (!validTabs.includes(tab)) tab = 'perfil';
        $('.settings-section').addClass('hidden');
        $('[data-settings-panel="' + tab + '"]').removeClass('hidden');
        $('.settings-nav-item')
            .removeClass('bg-primary/10 text-primary font-semibold')
            .addClass('text-muted-foreground font-medium hover:bg-accent hover:text-foreground');
        $('[data-settings-tab="' + tab + '"]')
            .addClass('bg-primary/10 text-primary font-semibold')
            .removeClass('text-muted-foreground font-medium hover:bg-accent hover:text-foreground');
        const url = new URL(window.location.href);
        url.searchParams.set('tab', tab);
        window.history.replaceState({}, '', url);
    }

    function openModal(id) {
        $('.settings-modal').addClass('hidden').removeClass('flex');
        $(id).removeClass('hidden').addClass('flex');
        $('body').addClass('overflow-hidden');
        lucide.createIcons();
    }

    function closeModals() {
        $('.settings-modal').addClass('hidden').removeClass('flex');
        $('body').removeClass('overflow-hidden');
    }

    $('.settings-nav-item').on('click', function () { showTab($(this).data('settings-tab')); });
    $('[data-go-tab]').on('click', function () { showTab($(this).data('go-tab')); });
    $('#btn-edit-profile').on('click', function () { openModal('#modal-edit-profile'); });
    $('#btn-change-password').on('click', function () { openModal('#modal-change-password'); });
    $('#btn-manage-sessions').on('click', function () { openModal('#modal-active-sessions'); });
    $('.js-settings-close-modal').on('click', closeModals);
    $('.settings-modal').on('mousedown', function (event) { if (event.target === this) closeModals(); });
    $(document).on('keydown', function (event) { if (event.key === 'Escape') closeModals(); });

    function renderTheme(theme) {
        $('.settings-theme-card').removeClass('border-primary bg-primary/5').addClass('border-border');
        $('.settings-theme-check').addClass('hidden');
        const $card = $('[data-theme-choice="' + theme + '"]');
        $card.addClass('border-primary bg-primary/5').removeClass('border-border');
        $card.find('.settings-theme-check').removeClass('hidden');
    }

    function renderDensity(density) {
        $('.settings-density-card').removeClass('border-primary bg-primary/5').addClass('border-border');
        $('[data-density-choice="' + density + '"]').addClass('border-primary bg-primary/5').removeClass('border-border');
        $('body').attr('data-density', density);
    }

    function applySelectedTheme(theme) {
        const systemDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
        $('html').toggleClass('dark', theme === 'dark' || (theme === 'system' && systemDark));
        if (theme === 'system') localStorage.removeItem(THEME_KEY);
        else localStorage.setItem(THEME_KEY, theme);
        $('#theme-value').val(theme);
        renderTheme(theme);
        lucide.createIcons();
    }

    /*
     * Importante: al cargar esta página NO se modifica la clase dark ni localStorage.
     * Se respeta exactamente el tema que head.html ya aplicó para todo el sistema.
     */
    const explicitTheme = localStorage.getItem(THEME_KEY);
    const savedTheme = String($page.data('saved-theme') || 'system');
    const initialTheme = explicitTheme === 'light' || explicitTheme === 'dark'
        ? explicitTheme
        : (['light', 'dark', 'system'].includes(savedTheme) ? savedTheme : 'system');
    $('#theme-value').val(initialTheme);
    renderTheme(initialTheme);
    renderDensity(String($page.data('density') || $('#density-value').val() || 'comfortable'));

    $('[data-theme-choice]').on('click', function () {
        applySelectedTheme($(this).data('theme-choice'));
        window.setTimeout(function () { document.getElementById('appearance-form').submit(); }, 120);
    });

    $('[data-density-choice]').on('click', function () {
        const density = $(this).data('density-choice');
        $('#density-value').val(density);
        renderDensity(density);
        window.setTimeout(function () { document.getElementById('appearance-form').submit(); }, 120);
    });

    $('#profile-file').on('change', function () {
        const file = this.files && this.files[0];
        if (!file) return;
        if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type) || file.size > 3 * 1024 * 1024) {
            this.value = '';
            Swal.fire({icon: 'warning', title: 'Imagen no válida', text: 'Selecciona una imagen JPG, PNG o WebP de máximo 3 MB.', confirmButtonColor: '#5eead4'});
            return;
        }
        const reader = new FileReader();
        reader.onload = function (event) {
            let $image = $('#profile-preview');
            if (!$image.length) {
                $image = $('<img id="profile-preview" class="h-full w-full object-cover" alt="Vista previa">');
                $('#profile-preview-fallback').before($image);
            }
            $image.attr('src', event.target.result).removeClass('hidden');
            $('#profile-preview-fallback').addClass('hidden');
        };
        reader.readAsDataURL(file);
    });

    const $bio = $('#profile-bio');
    function updateBioCounter() { $('#bio-count').text(($bio.val() || '').length + '/500'); }
    $bio.on('input', updateBioCounter); updateBioCounter();

    $('.js-toggle-password').on('click', function () {
        const $input = $('#' + $(this).data('target'));
        const show = $input.attr('type') === 'password';
        $input.attr('type', show ? 'text' : 'password');
        $(this).find('svg').attr('data-lucide', show ? 'eye-off' : 'eye');
        lucide.createIcons();
    });

    function passwordStrength(password) {
        let score = 0;
        if (password.length >= 12) score++;
        if (/[A-ZÁÉÍÓÚÑ]/.test(password)) score++;
        if (/\d/.test(password)) score++;
        if (/[^A-Za-z0-9]/.test(password)) score++;
        return score;
    }

    $('#new-password').on('input', function () {
        const points = passwordStrength($(this).val() || '');
        const widths = ['w-0', 'w-1/4', 'w-1/2', 'w-3/4', 'w-full'];
        const colors = ['bg-destructive', 'bg-destructive', 'bg-amber-500', 'bg-primary', 'bg-emerald-500'];
        const labels = ['Muy débil', 'Débil', 'Aceptable', 'Buena', 'Contraseña fuerte'];
        $('#password-strength-bar')
            .removeClass('w-0 w-1/4 w-1/2 w-3/4 w-full bg-destructive bg-amber-500 bg-primary bg-emerald-500')
            .addClass(widths[points] + ' ' + colors[points]);
        $('#password-strength-label').text(labels[points]);
    });

    showTab(String($page.data('tab') || 'perfil'));
    const modal = String($page.data('open-modal') || '');
    if (modal === 'perfil') openModal('#modal-edit-profile');
    if (modal === 'password') openModal('#modal-change-password');

    const success = $page.data('success-message');
    const error = $page.data('error-message');
    if (success) Swal.fire({icon: 'success', title: 'Listo', text: success, timer: 2400, showConfirmButton: false});
    if (error) Swal.fire({icon: 'error', title: 'No fue posible completar la acción', text: error});
});
