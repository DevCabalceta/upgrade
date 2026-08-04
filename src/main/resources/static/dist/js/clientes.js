(function ($) {
    'use strict';

    const VIEW_STORAGE_KEY = 'upgrade-client-view';
    let searchTimer;

    function createIcons() {
        if (window.lucide) {
            window.lucide.createIcons();
        }
    }

    function showModal(selector) {
        $('.client-card-menu').addClass('hidden');
        $('.js-client-card-menu-toggle').attr('aria-expanded', 'false');
        $(selector).removeClass('hidden').addClass('flex').attr('aria-hidden', 'false');
        $('body').addClass('overflow-hidden');
        createIcons();
    }

    function closeModal($modal) {
        $modal.addClass('hidden').removeClass('flex').attr('aria-hidden', 'true');
        if (!$('.client-modal.flex').length) {
            $('body').removeClass('overflow-hidden');
        }
    }

    function closeAllModals() {
        $('.client-modal').each(function () {
            closeModal($(this));
        });
    }

    function valueOrDash(value) {
        return value && String(value).trim() ? value : '—';
    }

    function clientFromRecord($record) {
        return $record.data();
    }

    function setText(selector, value) {
        $(selector).text(valueOrDash(value));
    }

    function fillDetail(client) {
        setText('#detail-client-avatar', client.clientIniciales);
        setText('#detail-client-title', client.clientNombreMostrado);
        setText('#detail-client-subtitle', `${valueOrDash(client.clientTipo)} · ${valueOrDash(client.clientIdentificacion)}`);
        setText('#detail-client-name', client.clientNombre);
        setText('#detail-client-email', client.clientCorreo);
        setText('#detail-client-phone', client.clientTelefono);
        setText('#detail-client-address', client.clientDireccion);
        setText('#detail-client-segment', client.clientSegmento);
        setText('#detail-client-date', client.clientFecha);
        setText('#detail-client-notes', client.clientNotas || 'Sin notas internas.');

        const active = String(client.clientActivo) === 'true';
        $('#detail-client-status')
            .text(active ? 'Activo' : 'Inactivo')
            .attr('class', active
                ? 'inline-flex rounded-md border border-emerald-500/30 bg-emerald-50 px-2 py-0.5 text-xs font-semibold text-emerald-700 dark:border-emerald-500/40 dark:bg-emerald-950/50 dark:text-emerald-400'
                : 'inline-flex rounded-md border border-red-500/30 bg-red-50 px-2 py-0.5 text-xs font-semibold text-red-700 dark:border-red-500/40 dark:bg-red-950/50 dark:text-red-400');
    }

    function fillEdit(client) {
        $('#edit-client-id').val(client.clientId);
        $('#edit-client-type').val(client.clientTipo);
        $('#edit-client-identification').val(client.clientIdentificacion);
        $('#edit-client-company').val(client.clientEmpresa || '');
        $('#edit-client-name').val(client.clientNombre);
        $('#edit-client-email').val(client.clientCorreo);
        $('#edit-client-phone').val(client.clientTelefono);
        $('#edit-client-address').val(client.clientDireccion || '');
        $('#edit-client-segment').val(client.clientSegmento || '');
        $('#edit-client-payment').val(client.clientCondicionesPago || '');
        $('#edit-client-notes').val(client.clientNotas || '');
        $('#edit-client-active').prop('checked', String(client.clientActivo) === 'true');
        $('#edit-client-subtitle').text(`Actualiza la información de ${valueOrDash(client.clientNombreMostrado)}.`);
    }

    function fillHistory(client) {
        $('#history-client-subtitle').text(valueOrDash(client.clientNombreMostrado));
        $('#history-client-date').text(`Registro: ${valueOrDash(client.clientFechaIso)}`);
    }

    function fillDelete(client) {
        $('#delete-client-id').val(client.clientId);
        $('#delete-client-name').text(`“${valueOrDash(client.clientNombreMostrado)}”`);
    }

    function handleAction(action, client) {
        if (action === 'detail') {
            fillDetail(client);
            showModal('#modal-client-detail');
        } else if (action === 'edit') {
            fillEdit(client);
            showModal('#modal-edit-client');
        } else if (action === 'history') {
            fillHistory(client);
            showModal('#modal-client-history');
        } else if (action === 'delete') {
            fillDelete(client);
            showModal('#modal-delete-client');
        }
    }

    function activateView(view) {
        const selected = view === 'cards' ? 'cards' : 'table';
        $('#client-table-view').toggleClass('hidden', selected !== 'table');
        $('#client-cards-view').toggleClass('hidden', selected !== 'cards');

        $('.client-view-button')
            .removeClass('bg-background text-foreground shadow')
            .addClass('text-muted-foreground');
        $(`[data-client-view="${selected}"]`)
            .addClass('bg-background text-foreground shadow')
            .removeClass('text-muted-foreground');
        localStorage.setItem(VIEW_STORAGE_KEY, selected);
    }

    function validateForm(form) {
        const $form = $(form);
        $form.find('.client-form-control').removeClass('border-destructive ring-1 ring-destructive');

        if (form.checkValidity()) {
            return true;
        }

        const firstInvalid = form.querySelector(':invalid');
        if (firstInvalid) {
            $(firstInvalid).addClass('border-destructive ring-1 ring-destructive').trigger('focus');
        }
        if (window.Swal) {
            Swal.fire({
                icon: 'warning',
                title: 'Revisa el formulario',
                text: 'Completa correctamente los campos obligatorios antes de continuar.',
                confirmButtonText: 'Entendido',
                confirmButtonColor: '#45d4b4'
            });
        }
        return false;
    }

    $(function () {
        createIcons();
        activateView(localStorage.getItem(VIEW_STORAGE_KEY) || 'table');

        $('.client-modal').attr('aria-hidden', 'true');

        $('.client-view-button').on('click', function () {
            activateView($(this).data('clientView'));
        });

        $('#btn-new-client').on('click', function () {
            showModal('#modal-new-client');
        });

        $(document).on('click', '.js-client-detail', function () {
            const client = clientFromRecord($(this).closest('.client-record'));
            handleAction('detail', client);
        });

        $(document).on('click', '.js-client-action', function (event) {
            event.preventDefault();
            event.stopPropagation();
            if ($(this).prop('disabled')) {
                return;
            }
            const client = clientFromRecord($(this).closest('.client-record'));
            handleAction($(this).data('clientAction'), client);
        });

        $(document).on('click', '.js-client-card-menu-toggle', function (event) {
            event.stopPropagation();
            const $button = $(this);
            const $menu = $button.siblings('.client-card-menu');
            $('.client-card-menu').not($menu).addClass('hidden');
            $('.js-client-card-menu-toggle').not($button).attr('aria-expanded', 'false');
            $menu.toggleClass('hidden');
            $button.attr('aria-expanded', String(!$menu.hasClass('hidden')));
        });

        $(document).on('click', function (event) {
            if (!$(event.target).closest('.client-card-menu, .js-client-card-menu-toggle').length) {
                $('.client-card-menu').addClass('hidden');
                $('.js-client-card-menu-toggle').attr('aria-expanded', 'false');
            }
        });

        $('.js-client-close-modal').on('click', function () {
            closeModal($(this).closest('.client-modal'));
        });

        $('.client-modal').on('mousedown', function (event) {
            if (event.target === this) {
                closeModal($(this));
            }
        });

        $(document).on('keydown', function (event) {
            if (event.key === 'Escape') {
                closeAllModals();
            }
        });

        $('#client-status-filter').on('change', function () {
            $('#client-filter-form').trigger('submit');
        });

        $('#client-search').on('input', function () {
            clearTimeout(searchTimer);
            searchTimer = setTimeout(function () {
                $('#client-filter-form').trigger('submit');
            }, 450);
        });

        $('#new-client-form, #edit-client-form').on('submit', function (event) {
            if (!validateForm(this)) {
                event.preventDefault();
            }
        });

        $('#delete-client-form').on('submit', function (event) {
            if (!window.Swal || $(this).data('confirmed')) {
                return;
            }
            event.preventDefault();
            const form = this;
            Swal.fire({
                icon: 'warning',
                title: '¿Desactivar este cliente?',
                text: 'El cliente dejará de estar disponible para nuevas operaciones.',
                showCancelButton: true,
                confirmButtonText: 'Sí, desactivar',
                cancelButtonText: 'Cancelar',
                confirmButtonColor: '#dc2626'
            }).then(function (result) {
                if (result.isConfirmed) {
                    $(form).data('confirmed', true);
                    form.submit();
                }
            });
        });

        const $page = $('#page-content');
        const successMessage = $page.attr('data-success-message');
        if (successMessage && window.Swal) {
            Swal.fire({
                icon: 'success',
                title: 'Operación completada',
                text: successMessage,
                timer: 3200,
                showConfirmButton: false
            });
        }

        const openModal = $page.attr('data-open-modal');
        if (openModal === 'nuevo') {
            showModal('#modal-new-client');
        } else if (openModal === 'editar') {
            showModal('#modal-edit-client');
        }
    });
})(jQuery);