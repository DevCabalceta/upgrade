(function ($) {
    'use strict';

    function createIcons() {
        if (window.lucide) window.lucide.createIcons();
    }

    function openModal(selector) {
        var $modal = $(selector);
        if (!$modal.length) return;
        $('.role-modal').addClass('hidden').removeClass('flex').attr('aria-hidden', 'true');
        $modal.removeClass('hidden').addClass('flex').attr('aria-hidden', 'false');
        $('body').addClass('overflow-hidden');
        setTimeout(function () { $modal.find('input:not([type="hidden"]), textarea, select, button').first().trigger('focus'); }, 50);
    }

    function closeModals() {
        $('.role-modal').addClass('hidden').removeClass('flex').attr('aria-hidden', 'true');
        $('body').removeClass('overflow-hidden');
    }

    function updateAccessCount() {
        var total = $('#form-inline-access .module-access-input:checked').length;
        $('#permission-enabled-count').text(total + (total === 1 ? ' módulo activo' : ' módulos activos'));
    }

    function validateForm(form) {
        if (form.checkValidity()) return true;
        $(form).find(':invalid').first().trigger('focus').addClass('border-destructive ring-1 ring-destructive');
        if (window.Swal) Swal.fire({icon: 'warning', title: 'Revisa el formulario', text: 'Completa correctamente los campos obligatorios.', confirmButtonColor: '#45d4b4'});
        return false;
    }

    $(function () {
        createIcons();
        $('.role-modal').attr('aria-hidden', 'true');
        updateAccessCount();

        $('#btn-new-role').on('click', function () { openModal('#modal-new-role'); });
        $('.js-role-close-modal').on('click', closeModals);
        $('.role-modal').on('mousedown', function (event) { if (event.target === this) closeModals(); });
        $(document).on('keydown', function (event) { if (event.key === 'Escape') closeModals(); });

        $('.js-role-menu-trigger').on('click', function (event) {
            event.preventDefault(); event.stopPropagation();
            var $menu = $(this).siblings('.js-role-menu');
            $('.js-role-menu').not($menu).addClass('hidden');
            $menu.toggleClass('hidden');
        });
        $(document).on('click', function () { $('.js-role-menu').addClass('hidden'); });
        $('.js-role-menu').on('click', function (event) { event.stopPropagation(); });

        $('#permission-role-select').on('change', function () {
            window.location.href = window.location.pathname + '?rol=' + encodeURIComponent($(this).val());
        });

        $('#permission-module-search').on('input', function () {
            var value = String($(this).val() || '').trim().toLowerCase();
            var visible = 0;
            $('.module-access-card').each(function () {
                var show = !value || String($(this).data('moduleName') || '').indexOf(value) !== -1;
                $(this).toggleClass('hidden', !show);
                if (show) visible++;
            });
            $('#permission-empty-state').toggleClass('hidden', visible !== 0);
        });

        $(document).on('change', '.module-access-input', updateAccessCount);
        $('#btn-enable-all').on('click', function () { $('.module-access-input').prop('checked', true); updateAccessCount(); });
        $('#btn-disable-all').on('click', function () { $('.module-access-input').prop('checked', false); updateAccessCount(); });

        $('#form-new-role, #form-edit-role').on('submit', function (event) {
            if (!validateForm(this)) event.preventDefault();
        });
        $('.role-control').on('input', function () { $(this).removeClass('border-destructive ring-1 ring-destructive'); });

        $('#form-delete-role').on('submit', function (event) {
            if ($(this).data('confirmed')) return;
            event.preventDefault();
            if ($('#delete-role-confirmation').val().trim().toUpperCase() !== 'ELIMINAR') {
                $('#delete-role-confirmation').addClass('border-destructive ring-1 ring-destructive').trigger('focus');
                if (window.Swal) Swal.fire({icon: 'warning', title: 'Confirmación incorrecta', text: 'Escribe ELIMINAR para continuar.', confirmButtonColor: '#45d4b4'});
                return;
            }
            var form = this;
            if (!window.Swal) { $(form).data('confirmed', true); form.submit(); return; }
            Swal.fire({icon: 'warning', title: '¿Eliminar definitivamente?', text: 'El rol dejará de estar disponible.', showCancelButton: true, confirmButtonText: 'Sí, eliminar', cancelButtonText: 'Cancelar', confirmButtonColor: '#dc2626'})
                .then(function (result) { if (result.isConfirmed) { $(form).data('confirmed', true); form.submit(); } });
        });
        $('#delete-role-confirmation').on('input', function () { $(this).removeClass('border-destructive ring-1 ring-destructive'); });

        var $page = $('#page-content');
        var success = $page.attr('data-success-message');
        if (success && window.Swal) Swal.fire({icon: 'success', title: 'Operación completada', text: success, timer: 3200, showConfirmButton: false});
        var modal = $page.attr('data-open-modal');
        if (modal === 'nuevo') openModal('#modal-new-role');
        if (modal === 'editar') openModal('#modal-edit-role');
        if (modal === 'ver') openModal('#modal-view-role');
        if (modal === 'accesos') openModal('#modal-access-role');
        if (modal === 'eliminar') openModal('#modal-delete-role');
    });
})(jQuery);
