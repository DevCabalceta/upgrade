(function ($) {
    'use strict';

    const VIEW_STORAGE_KEY = 'upgrade-collaborator-view';
    let searchTimer;

    function createIcons() {
        if (window.lucide) {
            window.lucide.createIcons();
        }
    }

    function showModal(selector) {
        $(selector).removeClass('hidden').addClass('flex').attr('aria-hidden', 'false');
        $('body').addClass('overflow-hidden');
        createIcons();
    }

    function closeModal($modal) {
        $modal.addClass('hidden').removeClass('flex').attr('aria-hidden', 'true');
        if (!$('.collaborator-modal.flex').length) {
            $('body').removeClass('overflow-hidden');
        }
    }

    function closeAllModals() {
        $('.collaborator-modal').each(function () {
            closeModal($(this));
        });
    }

    function collaboratorFromRecord($record) {
        return $record.data();
    }

    function valueOrDash(value) {
        return value === null || value === undefined || String(value).trim() === '' ? '—' : String(value);
    }

    function setText(selector, value) {
        $(selector).text(valueOrDash(value));
    }

    function statusClasses(status) {
        const base = 'w-fit rounded-md border px-2.5 py-0.5 text-xs font-semibold';
        if (status === 'DISPONIBLE') {
            return base + ' border-emerald-500/30 bg-emerald-50 text-emerald-700 dark:bg-emerald-950/50 dark:text-emerald-400';
        }
        if (status === 'EN_EVENTO') {
            return base + ' border-primary/30 bg-primary/10 text-primary';
        }
        return base + ' border-border bg-muted text-muted-foreground';
    }

    function formatDate(value) {
        if (!value) {
            return '—';
        }
        const date = new Date(String(value) + 'T00:00:00');
        return Number.isNaN(date.getTime())
            ? valueOrDash(value)
            : new Intl.DateTimeFormat('es-CR', {day: '2-digit', month: 'short', year: 'numeric'}).format(date);
    }

    function fillDetail(item) {
        setText('#detail-collaborator-initials', item.initials);
        setText('#detail-collaborator-name', item.fullName);
        setText('#detail-collaborator-username', '@' + valueOrDash(item.username));
        setText('#detail-collaborator-subtitle', valueOrDash(item.position) + ' · ' + valueOrDash(item.department));
        setText('#detail-collaborator-email', item.email);
        setText('#detail-collaborator-phone', item.phone);
        setText('#detail-collaborator-joined', formatDate(item.joined));
        setText('#detail-collaborator-department', item.department);
        setText('#detail-collaborator-role', item.roleName);
        setText('#detail-collaborator-events', item.events);
        $('#detail-collaborator-status').attr('class', statusClasses(item.status)).text(valueOrDash(item.statusLabel));
    }

    function fillEdit(item) {
        $('#edit-collaborator-id').val(item.collaboratorId);
        $('#edit-collaborator-first-name').val(item.firstName || '');
        $('#edit-collaborator-last-name').val(item.lastName || '');
        $('#edit-collaborator-username').val(item.username || '');
        $('#edit-collaborator-email').val(item.email || '');
        $('#edit-collaborator-phone').val(item.phone || '');
        $('#edit-collaborator-role').val(item.roleId || '');
        $('#edit-collaborator-position').val(item.positionId || '');
        $('#edit-collaborator-department').val(item.departmentId || '');
        $('#edit-collaborator-status').val(item.status || 'DISPONIBLE');
        $('#edit-collaborator-subtitle').text('Actualiza la información de ' + valueOrDash(item.fullName) + '.');
    }

    function fillRole(item, preserveSelection) {
        $('#role-collaborator-id').val(item.collaboratorId);
        $('#role-collaborator-subtitle').text('Selecciona el rol principal de ' + valueOrDash(item.fullName) + '.');
        if (!preserveSelection) {
            $('#role-collaborator-form input[name="rolId"]').prop('checked', false)
                .filter('[value="' + item.roleId + '"]').prop('checked', true);
        }
    }

    function fillDelete(item, preserveConfirmation) {
        $('#delete-collaborator-id').val(item.collaboratorId);
        $('#delete-collaborator-name').text('“' + valueOrDash(item.fullName) + '”');
        if (!preserveConfirmation) {
            $('#delete-collaborator-confirmation').val('');
        }
        updateDeleteState();
    }

    function handleAction(action, item) {
        if (action === 'detail') {
            fillDetail(item);
            showModal('#modal-collaborator-detail');
        } else if (action === 'edit') {
            fillEdit(item);
            showModal('#modal-edit-collaborator');
        } else if (action === 'role') {
            fillRole(item, false);
            showModal('#modal-role-collaborator');
        } else if (action === 'delete') {
            fillDelete(item, false);
            showModal('#modal-delete-collaborator');
            setTimeout(function () {
                $('#delete-collaborator-confirmation').trigger('focus');
            }, 100);
        }
    }

    function activateView(view) {
        const selected = view === 'cards' ? 'cards' : 'table';
        $('#collaborator-table-view').toggleClass('hidden', selected !== 'table');
        $('#collaborator-cards-view').toggleClass('hidden', selected !== 'cards');
        $('.collaborator-view-button').removeClass('bg-background text-foreground shadow').addClass('text-muted-foreground');
        $('[data-collaborator-view="' + selected + '"]').addClass('bg-background text-foreground shadow').removeClass('text-muted-foreground');
        localStorage.setItem(VIEW_STORAGE_KEY, selected);
    }

    function validateForm(form) {
        const $form = $(form);
        $form.find('.collaborator-form-control').removeClass('border-destructive ring-1 ring-destructive');
        if (!form.checkValidity()) {
            const firstInvalid = form.querySelector(':invalid');
            if (firstInvalid) {
                $(firstInvalid).addClass('border-destructive ring-1 ring-destructive').trigger('focus');
            }
            showValidationWarning('Completa correctamente los campos obligatorios.');
            return false;
        }
        if (form.id === 'new-collaborator-form') {
            const password = $('#new-collaborator-password').val();
            const confirmation = $('#new-collaborator-confirm-password').val();
            if (password !== confirmation) {
                $('#new-collaborator-confirm-password').addClass('border-destructive ring-1 ring-destructive').trigger('focus');
                showValidationWarning('Las contraseñas no coinciden.');
                return false;
            }
            if (!/[a-z]/.test(password) || !/[A-Z]/.test(password) || !/\d/.test(password)) {
                $('#new-collaborator-password').addClass('border-destructive ring-1 ring-destructive').trigger('focus');
                showValidationWarning('La contraseña debe incluir mayúscula, minúscula y número.');
                return false;
            }
        }
        return true;
    }

    function showValidationWarning(message) {
        if (window.Swal) {
            Swal.fire({
                icon: 'warning',
                title: 'Revisa el formulario',
                text: message,
                confirmButtonColor: '#45d4b4'
            });
        }
    }

    function updateDeleteState() {
        const valid = $('#delete-collaborator-confirmation').val().trim() === 'ELIMINAR';
        $('#delete-collaborator-submit').prop('disabled', !valid);
        $('#delete-collaborator-help')
            .toggleClass('text-emerald-500', valid)
            .toggleClass('text-muted-foreground', !valid)
            .text(valid ? 'Confirmación correcta. Ya puedes desactivar la cuenta.' : 'El botón se habilitará al escribir la palabra completa.');
    }

    function recordById(id) {
        return $('.collaborator-record').filter(function () {
            return String($(this).data('collaboratorId')) === String(id);
        }).first();
    }

    $(function () {
        createIcons();
        $('.collaborator-modal').attr('aria-hidden', 'true');
        activateView(localStorage.getItem(VIEW_STORAGE_KEY) || 'table');

        $('#btn-new-collaborator').on('click', function () {
            const form = document.getElementById('new-collaborator-form');
            if (form) {
                form.reset();
            }
            showModal('#modal-new-collaborator');
        });

        $('.collaborator-view-button').on('click', function () {
            activateView($(this).data('collaboratorView'));
        });

        $('#btn-toggle-collaborator-filters').on('click', function () {
            $('#collaborator-filter-options').toggleClass('hidden').toggleClass('flex');
        });

        $('#collaborator-status-filter, #collaborator-role-filter').on('change', function () {
            $('#collaborator-filter-form').trigger('submit');
        });

        $('#collaborator-search').on('input', function () {
            clearTimeout(searchTimer);
            searchTimer = setTimeout(function () {
                $('#collaborator-filter-form').trigger('submit');
            }, 450);
        });

        $(document).on('click', '.collaborator-record', function (event) {
            if ($(event.target).closest('.collaborator-actions, .collaborator-card-menu, .js-collaborator-menu-toggle, .js-collaborator-action').length) {
                return;
            }
            handleAction('detail', collaboratorFromRecord($(this)));
        });

        $(document).on('click', '.js-collaborator-action', function (event) {
            event.preventDefault();
            event.stopPropagation();
            handleAction($(this).data('collaboratorAction'), collaboratorFromRecord($(this).closest('.collaborator-record')));
        });

        $(document).on('click', '.js-collaborator-menu-toggle', function (event) {
            event.stopPropagation();
            const $button = $(this);
            const $menu = $button.siblings('.collaborator-card-menu');
            $('.collaborator-card-menu').not($menu).addClass('hidden');
            $('.js-collaborator-menu-toggle').not($button).attr('aria-expanded', 'false');
            $menu.toggleClass('hidden');
            $button.attr('aria-expanded', String(!$menu.hasClass('hidden')));
        });

        $(document).on('click', function (event) {
            if (!$(event.target).closest('.collaborator-card-menu, .js-collaborator-menu-toggle').length) {
                $('.collaborator-card-menu').addClass('hidden');
                $('.js-collaborator-menu-toggle').attr('aria-expanded', 'false');
            }
        });

        $('.js-collaborator-close-modal').on('click', function () {
            closeModal($(this).closest('.collaborator-modal'));
        });

        $('.collaborator-modal').on('mousedown', function (event) {
            if (event.target === this) {
                closeModal($(this));
            }
        });

        $(document).on('keydown', function (event) {
            if (event.key === 'Escape') {
                closeAllModals();
            }
        });

        $('.js-toggle-password').on('click', function () {
            const $input = $($(this).data('passwordTarget'));
            $input.attr('type', $input.attr('type') === 'password' ? 'text' : 'password');
        });

        $('#new-collaborator-form, #edit-collaborator-form').on('submit', function (event) {
            if (!validateForm(this)) {
                event.preventDefault();
            }
        });

        $('#delete-collaborator-confirmation').on('input', function () {
            this.value = this.value.toUpperCase();
            updateDeleteState();
        });

        $('#delete-collaborator-form').on('submit', function (event) {
            if ($('#delete-collaborator-confirmation').val().trim() !== 'ELIMINAR') {
                event.preventDefault();
                $('#delete-collaborator-confirmation').trigger('focus');
            }
        });

        const $page = $('#page-content');
        const success = $page.attr('data-success-message');
        const error = $page.attr('data-error-message');
        const warning = $page.attr('data-warning-message');
        const mail = $page.attr('data-mail-message');
        if (success && warning && window.Swal) {
            Swal.fire({icon: 'warning', title: 'Cuenta creada con advertencia', text: success + ' ' + warning, confirmButtonColor: '#45d4b4'});
        } else if (success && window.Swal) {
            Swal.fire({icon: 'success', title: 'Operación completada', text: mail ? success + ' ' + mail : success, timer: 4200, showConfirmButton: false});
        } else if (error && window.Swal) {
            Swal.fire({icon: 'error', title: 'No fue posible completar la acción', text: error, confirmButtonColor: '#45d4b4'});
        } else if (warning && window.Swal) {
            Swal.fire({icon: 'warning', title: 'Operación completada con advertencia', text: warning, confirmButtonColor: '#45d4b4'});
        }

        const openModal = $page.attr('data-open-modal');
        const modalId = $page.attr('data-collaborator-modal-id');
        const $record = modalId ? recordById(modalId) : $();
        const item = $record.length ? collaboratorFromRecord($record) : null;
        if (openModal === 'nuevo') {
            showModal('#modal-new-collaborator');
        } else if (openModal === 'editar') {
            if (item) {
                $('#edit-collaborator-subtitle').text('Corrige la información de ' + valueOrDash(item.fullName) + '.');
            }
            showModal('#modal-edit-collaborator');
        } else if (openModal === 'rol') {
            if (item) {
                fillRole(item, true);
            }
            showModal('#modal-role-collaborator');
        } else if (openModal === 'eliminar') {
            if (item) {
                fillDelete(item, true);
            }
            showModal('#modal-delete-collaborator');
        }
    });
})(jQuery);
