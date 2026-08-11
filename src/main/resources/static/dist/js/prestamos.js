(function ($) {
    'use strict';

    $(function () {
        const $page = $('#page-content');
        const defaultReminder = 'Hola, te recordamos que el equipo en préstamo se encuentra vencido. Por favor confirma la fecha de devolución.';
        let searchTimer = null;

        function refreshIcons() {
            if (window.lucide) {
                window.lucide.createIcons();
            }
        }

        function openModal(selector) {
            $('.loan-modal').addClass('hidden').removeClass('flex');
            $(selector).removeClass('hidden').addClass('flex');
            $('body').addClass('overflow-hidden');
            refreshIcons();
        }

        function closeModals() {
            $('.loan-modal').addClass('hidden').removeClass('flex');
            $('body').removeClass('overflow-hidden');
        }

        function recordFromRow($row) {
            return {
                id: String($row.attr('data-loan-id') || ''),
                folio: $row.attr('data-loan-folio') || 'Préstamo',
                client: $row.attr('data-loan-client') || '—',
                contact: $row.attr('data-loan-contact') || '—',
                equipment: $row.attr('data-loan-equipment') || '—',
                equipmentCode: $row.attr('data-loan-equipment-code') || '—',
                quantity: $row.attr('data-loan-quantity') || '—',
                outDate: $row.attr('data-loan-out-date') || '',
                backDate: $row.attr('data-loan-back-date') || '',
                realDate: $row.attr('data-loan-real-date') || '',
                state: $row.attr('data-loan-state') || '—',
                baseState: $row.attr('data-loan-base-state') || '',
                responsible: $row.attr('data-loan-responsible') || '—',
                outCondition: $row.attr('data-loan-out-condition') || '—',
                returnCondition: $row.attr('data-loan-return-condition') || '—',
                notes: $row.attr('data-loan-notes') || 'Sin observaciones.',
                returnNotes: $row.attr('data-loan-return-notes') || '',
                overdue: String($row.attr('data-loan-overdue')) === 'true',
                delay: Number($row.attr('data-loan-delay') || 0),
                lastNotification: $row.attr('data-loan-last-notification') || 'Nunca'
            };
        }

        function findRecord(id) {
            let record = null;
            $('.loan-record').each(function () {
                const candidate = recordFromRow($(this));
                if (candidate.id === String(id)) {
                    record = candidate;
                    return false;
                }
            });
            return record;
        }

        function displayDate(value) {
            if (!value) {
                return '—';
            }
            const parts = value.substring(0, 10).split('-');
            if (parts.length !== 3) {
                return value;
            }
            return parts[2] + '/' + parts[1] + '/' + parts[0];
        }

        function setText(id, value) {
            const element = document.getElementById(id);
            if (element) {
                element.textContent = value || '—';
            }
        }

        function showDetail(record) {
            setText('loan-detail-title', 'Préstamo ' + record.folio);
            setText('loan-detail-equipo', record.equipment + ' · ' + record.equipmentCode);
            setText('loan-detail-cantidad', record.quantity + ' unidad(es)');
            setText('loan-detail-cliente', record.client);
            setText('loan-detail-responsable', record.responsible);
            setText('loan-detail-fecha-salida', displayDate(record.outDate));
            setText('loan-detail-devolución-estimada', displayDate(record.backDate));
            setText('loan-detail-condición-salida', record.outCondition);
            setText('loan-detail-condición-devolución', record.returnCondition || 'Pendiente');
            setText('loan-detail-contacto', record.contact);
            setText('loan-detail-última-notificación', record.lastNotification === 'Nunca' ? 'Nunca' : record.lastNotification.replace('T', ' '));
            setText('loan-detail-notes', record.returnNotes
                ? record.notes + ' Devolución: ' + record.returnNotes
                : record.notes);

            const $state = $('#loan-detail-state');
            $state.text(record.state)
                .removeClass('border-red-500/30 bg-red-50 text-red-700 dark:bg-red-950/50 dark:text-red-400 border-primary/30 bg-primary/10 text-primary border-emerald-500/30 bg-emerald-50 text-emerald-700 dark:bg-emerald-950/50 dark:text-emerald-400')
                .addClass(record.overdue
                    ? 'border-red-500/30 bg-red-50 text-red-700 dark:bg-red-950/50 dark:text-red-400'
                    : record.baseState === 'ACTIVO'
                        ? 'border-primary/30 bg-primary/10 text-primary'
                        : 'border-emerald-500/30 bg-emerald-50 text-emerald-700 dark:bg-emerald-950/50 dark:text-emerald-400');

            setText('loan-detail-deadline', record.realDate
                ? 'Devuelto el ' + displayDate(record.realDate)
                : (record.overdue ? record.delay + ' día(s) de retraso' : 'Vence el ' + displayDate(record.backDate)));
            openModal('#modal-loan-detail');
        }

        function prepareReturn(record, resetValues) {
            $('#return-loan-id').val(record.id);
            $('#return-loan-folio').text(record.folio);
            $('#return-loan-summary').text(record.equipment + ' · ' + record.quantity + ' unidad(es) · ' + record.client);
            if (resetValues) {
                $('#return-loan-date').val(new Date().toLocaleDateString('en-CA'));
                $('#loan-return-form select[name="condicionDevolucion"]').val('OPTIMO');
                $('#loan-return-form textarea[name="observaciones"]').val('');
            }
            openModal('#modal-loan-return');
        }

        function prepareNotification(record, resetValues) {
            $('#notify-loan-id').val(record.id);
            $('#notify-loan-title').text('Préstamo vencido · ' + record.folio);
            $('#notify-loan-summary').html(
                '<strong>' + $('<div>').text(record.equipment + ' — ' + record.client).html() + '</strong>' +
                '<div class="mt-1 text-xs text-destructive/90">Venció el ' + displayDate(record.backDate) +
                ' · ' + record.delay + ' día(s) de retraso</div>'
            );
            if (resetValues) {
                $('#loan-notify-form textarea[name="mensaje"]').val(defaultReminder);
            }
            openModal('#modal-loan-notify');
        }

        function markInvalid($control, invalid) {
            $control.toggleClass('border-destructive ring-1 ring-destructive/30', invalid);
        }

        function validateForm(form) {
            let firstInvalid = null;
            $(form).find('.loan-form-control').each(function () {
                const invalid = !this.checkValidity();
                markInvalid($(this), invalid);
                if (invalid && !firstInvalid) {
                    firstInvalid = this;
                }
            });
            if (firstInvalid) {
                firstInvalid.reportValidity();
                firstInvalid.focus();
                return false;
            }
            return true;
        }

        $('#btn-new-loan').on('click', function () {
            openModal('#modal-new-loan');
        });

        $('.js-loan-close-modal').on('click', closeModals);
        $('.loan-modal').on('mousedown', function (event) {
            if (event.target === this) {
                closeModals();
            }
        });
        $(document).on('keydown', function (event) {
            if (event.key === 'Escape') {
                closeModals();
            }
        });

        $('.loan-record').on('click', function (event) {
            if ($(event.target).closest('.loan-actions').length) {
                return;
            }
            showDetail(recordFromRow($(this)));
        });

        $('.js-loan-action').on('click', function (event) {
            event.stopPropagation();
            const record = recordFromRow($(this).closest('.loan-record'));
            const action = $(this).attr('data-loan-action');
            if (action === 'detail') {
                showDetail(record);
            } else if (action === 'return') {
                prepareReturn(record, true);
            } else if (action === 'notify') {
                prepareNotification(record, true);
            }
        });

        $('#new-loan-equipment').on('change', function () {
            const stock = Number($(this).find(':selected').attr('data-stock') || 0);
            const $quantity = $('#new-loan-quantity');
            if (stock > 0) {
                $quantity.attr('max', stock);
                $('#new-loan-stock-help').text(stock + ' unidad(es) disponibles.');
            } else {
                $quantity.removeAttr('max');
                $('#new-loan-stock-help').text('');
            }
        }).trigger('change');

        $('#new-loan-client').on('change', function () {
            const email = $(this).find(':selected').attr('data-email') || '';
            const $contact = $('#new-loan-contact');
            if (!$contact.val() || $contact.attr('data-autofilled') === 'true') {
                $contact.val(email).attr('data-autofilled', 'true');
            }
        });
        $('#new-loan-contact').on('input', function () {
            $(this).attr('data-autofilled', 'false');
        });

        $('#new-loan-form').on('submit', function (event) {
            const form = this;
            const $out = $(form).find('[name="fechaSalida"]');
            const $back = $(form).find('[name="fechaDevolucionEstimada"]');
            markInvalid($back, Boolean($out.val() && $back.val() && $back.val() < $out.val()));
            if ($out.val() && $back.val() && $back.val() < $out.val()) {
                event.preventDefault();
                $back[0].setCustomValidity('La devolución estimada no puede ser anterior a la salida.');
                $back[0].reportValidity();
                return;
            }
            $back[0].setCustomValidity('');
            if (!validateForm(form)) {
                event.preventDefault();
            }
        });

        $('#loan-return-form, #loan-notify-form').on('submit', function (event) {
            if (!validateForm(this)) {
                event.preventDefault();
            }
        });

        $('.loan-form-control').on('input change', function () {
            this.setCustomValidity('');
            markInvalid($(this), !this.checkValidity());
        });

        $('#btn-toggle-loan-filters').on('click', function () {
            $('#loan-filter-options').toggleClass('hidden flex');
        });
        $('#loan-state-filter, #loan-date-filter').on('change', function () {
            $('#loan-filter-form').trigger('submit');
        });
        $('#loan-search').on('input', function () {
            window.clearTimeout(searchTimer);
            searchTimer = window.setTimeout(function () {
                $('#loan-filter-form').trigger('submit');
            }, 450);
        });

        function showFeedback() {
            if (!window.Swal) {
                return;
            }
            const success = $page.attr('data-success-message');
            const error = $page.attr('data-error-message');
            const warning = $page.attr('data-warning-message');
            const mail = $page.attr('data-mail-message');
            if (error) {
                window.Swal.fire({icon: 'error', title: 'No fue posible completar la acción', text: error, confirmButtonColor: '#5eead4'});
            } else if (warning) {
                window.Swal.fire({icon: 'warning', title: success || 'Acción completada con advertencia', text: warning, confirmButtonColor: '#5eead4'});
            } else if (success) {
                window.Swal.fire({icon: 'success', title: success, text: mail || '', confirmButtonColor: '#5eead4'});
            }
        }

        const modalToOpen = $page.attr('data-open-modal');
        if (modalToOpen === 'nuevo') {
            openModal('#modal-new-loan');
        } else if (modalToOpen === 'devolucion') {
            const record = findRecord($('#return-loan-id').val());
            if (record) {
                prepareReturn(record, false);
            }
        } else if (modalToOpen === 'notificacion') {
            const record = findRecord($('#notify-loan-id').val());
            if (record) {
                prepareNotification(record, false);
            }
        }

        showFeedback();
        refreshIcons();
    });
})(jQuery);
