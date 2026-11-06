(function () {
          const inputs = document.querySelectorAll('[data-money-input]');

          function parseAmount(value) {
                    const raw = String(value || '').trim();
                    const negative = raw.startsWith('-');
                    const cleaned = raw.replace(/[^0-9.,]/g, '');
                    const lastComma = cleaned.lastIndexOf(',');
                    const lastPoint = cleaned.lastIndexOf('.');
                    let decimalSeparator = null;

                    if (lastComma >= 0 && lastPoint >= 0) {
                              decimalSeparator = lastComma > lastPoint ? ',' : '.';
                    } else if (lastComma >= 0) {
                              const commaCount = (cleaned.match(/,/g) || []).length;
                              const trailingDigits = cleaned.length - lastComma - 1;
                              const groupedCommas = /^\d{1,3}(,\d{3})+$/.test(cleaned);
                              if (!groupedCommas && !(commaCount === 1 && trailingDigits === 3)) {
                                        decimalSeparator = ',';
                              }
                    } else if (lastPoint >= 0) {
                              const pointCount = (cleaned.match(/\./g) || []).length;
                              const trailingDigits = cleaned.length - lastPoint - 1;
                              const groupedPoints = /^\d{1,3}(\.\d{3})+$/.test(cleaned);
                              if (!groupedPoints && trailingDigits <= 2) {
                                        decimalSeparator = '.';
                              } else if (trailingDigits === 0) {
                                        decimalSeparator = '.';
                              } else if (pointCount === 1 && trailingDigits <= 2) {
                                        decimalSeparator = '.';
                              }
                    }

                    const separatorIndex = decimalSeparator === null
                              ? -1
                              : cleaned.lastIndexOf(decimalSeparator);
                    let integer = (separatorIndex < 0 ? cleaned : cleaned.slice(0, separatorIndex))
                              .replace(/\D/g, '');
                    let fraction = separatorIndex < 0
                              ? ''
                              : cleaned.slice(separatorIndex + 1).replace(/\D/g, '').slice(0, 2);

                    integer = integer.replace(/^0+(?=\d)/, '');
                    if (!integer && (fraction || separatorIndex >= 0)) {
                              integer = '0';
                    }

                    return {
                              negative: negative && (integer !== '' || fraction !== ''),
                              integer,
                              fraction,
                              decimal: separatorIndex >= 0
                    };
          }

          function formatAmount(amount, trimFraction) {
                    if (!amount.integer && !amount.fraction && !amount.decimal) {
                              return amount.negative ? '-' : '';
                    }

                    let fraction = amount.fraction;
                    if (trimFraction) {
                              fraction = fraction.replace(/0+$/, '');
                    }

                    const integer = amount.integer.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
                    const sign = amount.negative ? '-' : '';
                    return sign + integer + (amount.decimal && (fraction || !trimFraction)
                              ? ',' + fraction
                              : '');
          }

          function caretAfterDigits(value, digitCount) {
                    if (digitCount === 0) {
                              return value.startsWith('-') ? 1 : 0;
                    }

                    let seen = 0;
                    for (let index = 0; index < value.length; index += 1) {
                              if (/\d/.test(value[index])) {
                                        seen += 1;
                                        if (seen === digitCount) {
                                                  return index + 1;
                                        }
                              }
                    }
                    return value.length;
          }

          function canonicalAmount(value) {
                    const amount = parseAmount(value);
                    if (!amount.integer && !amount.fraction) {
                              return '';
                    }
                    return (amount.negative ? '-' : '') + (amount.integer || '0')
                              + (amount.fraction ? '.' + amount.fraction : '');
          }

          inputs.forEach(function (input) {
                    input.value = formatAmount(parseAmount(input.value), true);

                    input.addEventListener('input', function () {
                              const caret = input.selectionStart || 0;
                              const prefix = parseAmount(input.value.slice(0, caret));
                              const formatted = formatAmount(parseAmount(input.value), false);
                              input.value = formatted;

                              let nextCaret;
                              if (prefix.decimal && formatted.includes(',')) {
                                        nextCaret = formatted.indexOf(',') + 1 + prefix.fraction.length;
                              } else {
                                        nextCaret = caretAfterDigits(formatted, prefix.integer.length);
                              }
                              input.setSelectionRange(nextCaret, nextCaret);
                    });

                    input.addEventListener('blur', function () {
                              input.value = formatAmount(parseAmount(input.value), true);
                    });

                    if (input.form) {
                              input.form.addEventListener('submit', function () {
                                        input.value = canonicalAmount(input.value);
                              });
                    }
          });
}());