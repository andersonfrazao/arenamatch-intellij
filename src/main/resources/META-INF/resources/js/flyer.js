(function (window, document) {
    "use strict";

    const WIDTH = 1080;
    const HEIGHT = 1350;
    const NEON = "#0ea5e9";
    const CYAN = "#22d3ee";
    const WHITE = "#f8fafc";
    let flyerBlob = null;
    let flyerFile = null;
    let previewUrl = null;

    function text(root, selector, fallback) {
        const element = root.querySelector(selector);
        const value = element ? element.textContent.trim() : "";
        return value || fallback;
    }

    function loadImage(source) {
        return new Promise(function (resolve) {
            if (!source) {
                resolve(null);
                return;
            }

            const image = new Image();
            image.crossOrigin = "anonymous";
            image.onload = function () { resolve(image); };
            image.onerror = function () { resolve(null); };
            image.src = source;
        });
    }

    function drawCover(ctx, image) {
        const scale = Math.max(WIDTH / image.width, HEIGHT / image.height);
        const width = image.width * scale;
        const height = image.height * scale;
        ctx.drawImage(image, (WIDTH - width) / 2, (HEIGHT - height) / 2, width, height);
    }

    function roundedRect(ctx, x, y, width, height, radius) {
        const r = Math.min(radius, width / 2, height / 2);
        ctx.beginPath();
        ctx.moveTo(x + r, y);
        ctx.arcTo(x + width, y, x + width, y + height, r);
        ctx.arcTo(x + width, y + height, x, y + height, r);
        ctx.arcTo(x, y + height, x, y, r);
        ctx.arcTo(x, y, x + width, y, r);
        ctx.closePath();
    }

    function panel(ctx, x, y, width, height) {
        ctx.save();
        roundedRect(ctx, x, y, width, height, 24);
        ctx.fillStyle = "rgba(2, 12, 27, 0.84)";
        ctx.fill();
        ctx.strokeStyle = "rgba(14, 165, 233, 0.72)";
        ctx.lineWidth = 3;
        ctx.stroke();
        ctx.restore();
    }

    function drawShieldImage(ctx, image, centerX, centerY, maxSize) {
        if (!image) {
            drawFallbackShield(ctx, centerX, centerY, maxSize);
            return;
        }

        const scale = Math.min(maxSize / image.width, maxSize / image.height);
        const width = image.width * scale;
        const height = image.height * scale;
        ctx.save();
        ctx.shadowColor = "rgba(0, 0, 0, 0.65)";
        ctx.shadowBlur = 24;
        ctx.shadowOffsetY = 10;
        ctx.drawImage(image, centerX - width / 2, centerY - height / 2, width, height);
        ctx.restore();
    }

    function drawFallbackShield(ctx, centerX, centerY, size) {
        const width = size * 0.72;
        const height = size * 0.88;
        ctx.save();
        ctx.translate(centerX, centerY);
        ctx.beginPath();
        ctx.moveTo(-width / 2, -height / 2);
        ctx.lineTo(width / 2, -height / 2);
        ctx.lineTo(width * 0.46, height * 0.12);
        ctx.quadraticCurveTo(width * 0.35, height * 0.40, 0, height / 2);
        ctx.quadraticCurveTo(-width * 0.35, height * 0.40, -width * 0.46, height * 0.12);
        ctx.closePath();
        ctx.fillStyle = "rgba(11, 52, 87, 0.96)";
        ctx.fill();
        ctx.strokeStyle = CYAN;
        ctx.lineWidth = 10;
        ctx.stroke();
        ctx.beginPath();
        ctx.moveTo(-width * 0.24, -height * 0.14);
        ctx.lineTo(width * 0.24, -height * 0.14);
        ctx.moveTo(0, -height * 0.34);
        ctx.lineTo(0, height * 0.24);
        ctx.strokeStyle = "rgba(34, 211, 238, 0.85)";
        ctx.lineWidth = 7;
        ctx.stroke();
        ctx.restore();
    }

    function splitLines(ctx, value, maxWidth) {
        const words = String(value || "").trim().split(/\s+/).filter(Boolean);
        const lines = [];
        let current = "";

        words.forEach(function (word) {
            const candidate = current ? current + " " + word : word;
            if (current && ctx.measureText(candidate).width > maxWidth) {
                lines.push(current);
                current = word;
            } else {
                current = candidate;
            }
        });
        if (current) {
            lines.push(current);
        }
        return lines;
    }

    function fitLines(ctx, value, maxWidth, maxLines, startSize, minSize, family) {
        let fontSize = startSize;
        let lines = [];
        while (fontSize >= minSize) {
            ctx.font = "900 " + fontSize + "px " + family;
            lines = splitLines(ctx, value, maxWidth);
            const linesFitWidth = lines.every(function (line) {
                return ctx.measureText(line).width <= maxWidth;
            });
            if (lines.length <= maxLines && linesFitWidth) {
                break;
            }
            fontSize -= 2;
        }

        if (lines.length > maxLines) {
            lines = lines.slice(0, maxLines);
            let last = lines[maxLines - 1];
            while (last.length > 1 && ctx.measureText(last + "…").width > maxWidth) {
                last = last.slice(0, -1);
            }
            lines[maxLines - 1] = last.trim() + "…";
        }
        return { fontSize: fontSize, lines: lines };
    }

    function drawCenteredLines(ctx, value, centerX, startY, maxWidth, maxLines, startSize, minSize, lineHeight) {
        const fitted = fitLines(ctx, value, maxWidth, maxLines, startSize, minSize, "Arial, sans-serif");
        ctx.save();
        ctx.font = "900 " + fitted.fontSize + "px Arial, sans-serif";
        ctx.fillStyle = WHITE;
        ctx.textAlign = "center";
        ctx.textBaseline = "middle";
        fitted.lines.forEach(function (line, index) {
            ctx.fillText(line.toUpperCase(), centerX, startY + index * lineHeight);
        });
        ctx.restore();
    }

    function drawAddress(ctx, value) {
        const fitted = fitLines(ctx, value, 830, 3, 37, 27, "Arial, sans-serif");
        ctx.save();
        ctx.font = "800 " + fitted.fontSize + "px Arial, sans-serif";
        ctx.fillStyle = WHITE;
        ctx.textAlign = "center";
        ctx.textBaseline = "middle";
        fitted.lines.forEach(function (line, index) {
            ctx.fillText(line, WIDTH / 2, 1018 + index * 46);
        });
        ctx.restore();
    }

    function safeFilename(value) {
        return String(value || "jogo")
            .normalize("NFD")
            .replace(/[\u0300-\u036f]/g, "")
            .toLowerCase()
            .replace(/[^a-z0-9]+/g, "-")
            .replace(/^-+|-+$/g, "")
            .slice(0, 55) || "jogo";
    }

    function canvasToBlob(canvas) {
        return new Promise(function (resolve, reject) {
            canvas.toBlob(function (blob) {
                if (blob) {
                    resolve(blob);
                } else {
                    reject(new Error("Não foi possível criar o arquivo do flyer."));
                }
            }, "image/png");
        });
    }

    function showError(message) {
        window.alert(message || "Não foi possível gerar o flyer. Tente novamente.");
    }

    async function generate(button) {
        if (!button || button.disabled) {
            return;
        }

        const match = button.closest(".item-lista");
        const data = match ? match.querySelector(".flyer-match-data") : null;
        if (!match || !data) {
            showError("Os dados da partida não foram encontrados.");
            return;
        }

        button.disabled = true;
        button.classList.add("ui-state-disabled");

        try {
            const homeName = text(data, ".flyer-home-name", "Mandante");
            const awayName = text(data, ".flyer-away-name", "Visitante");
            const weekday = text(data, ".flyer-weekday", "Data definida");
            const date = text(data, ".flyer-date", "");
            const time = text(data, ".flyer-time", "--:--");
            const address = text(data, ".flyer-address", "Local não informado");
            const homeElement = match.querySelector("img.flyer-shield-home");
            const awayElement = match.querySelector("img.flyer-shield-away");

            const images = await Promise.all([
                loadImage(button.dataset.flyerBackground),
                loadImage(homeElement ? homeElement.src : null),
                loadImage(awayElement ? awayElement.src : null),
                loadImage(button.dataset.flyerLogo)
            ]);
            if (!images[0]) {
                throw new Error("O fundo do flyer não pôde ser carregado.");
            }

            const canvas = document.createElement("canvas");
            canvas.width = WIDTH;
            canvas.height = HEIGHT;
            const ctx = canvas.getContext("2d");
            drawCover(ctx, images[0]);

            const shade = ctx.createLinearGradient(0, 0, 0, HEIGHT);
            shade.addColorStop(0, "rgba(10, 20, 35, 0.8)");
            shade.addColorStop(1, "rgba(10, 20, 35, 0.9)");
            ctx.fillStyle = shade;
            ctx.fillRect(0, 0, WIDTH, HEIGHT);

            ctx.textAlign = "center";
            ctx.textBaseline = "middle";
            ctx.fillStyle = WHITE;
            ctx.font = "900 66px Arial, sans-serif";
            ctx.fillText("JOGO MARCADO", WIDTH / 2, 105);
            const brandLine = ctx.createLinearGradient(290, 0, 790, 0);
            brandLine.addColorStop(0, NEON);
            brandLine.addColorStop(1, CYAN);
            ctx.fillStyle = brandLine;
            ctx.fillRect(290, 153, 500, 6);

            drawShieldImage(ctx, images[1], 290, 365, 270);
            drawShieldImage(ctx, images[2], 790, 365, 270);

            ctx.fillStyle = CYAN;
            ctx.font = "900 94px Arial, sans-serif";
            ctx.fillText("X", WIDTH / 2, 375);

            drawCenteredLines(ctx, homeName, 290, 555, 390, 2, 43, 29, 48);
            drawCenteredLines(ctx, awayName, 790, 555, 390, 2, 43, 29, 48);

            panel(ctx, 70, 680, 940, 205);
            ctx.fillStyle = NEON;
            ctx.font = "800 25px Arial, sans-serif";
            ctx.fillText("DATA", 265, 725);
            ctx.fillText("HORÁRIO", 815, 725);
            ctx.fillStyle = WHITE;
            ctx.font = "900 35px Arial, sans-serif";
            ctx.fillText(weekday.toUpperCase(), 265, 775);
            ctx.font = "900 46px Arial, sans-serif";
            ctx.fillText(date, 265, 830);
            ctx.font = "900 70px Arial, sans-serif";
            ctx.fillText(time, 815, 795);
            ctx.fillStyle = "rgba(14, 165, 233, 0.62)";
            ctx.fillRect(539, 715, 3, 135);

            panel(ctx, 70, 920, 940, 230);
            ctx.fillStyle = NEON;
            ctx.font = "800 25px Arial, sans-serif";
            ctx.fillText("LOCAL DO JOGO", WIDTH / 2, 965);
            drawAddress(ctx, address);

            if (images[3]) {
                const logoWidth = 300;
                const logoHeight = logoWidth * (images[3].height / images[3].width);
                ctx.drawImage(images[3], (WIDTH - logoWidth) / 2, 1255, logoWidth, logoHeight);
            }

            flyerBlob = await canvasToBlob(canvas);
            const name = "arena-match-" + safeFilename(homeName) + "-x-" + safeFilename(awayName) + "-" + date.replace(/\D/g, "-") + ".png";
            flyerFile = new File([flyerBlob], name, { type: "image/png" });

            if (previewUrl) {
                URL.revokeObjectURL(previewUrl);
            }
            previewUrl = URL.createObjectURL(flyerBlob);
            const preview = document.getElementById("flyerPreviewImage");
            if (preview) {
                preview.src = previewUrl;
                preview.alt = "Flyer de " + homeName + " contra " + awayName;
            }

            const shareButton = document.getElementById("flyerShareButton");
            const isMobileViewport = window.matchMedia("(max-width: 640px), (pointer: coarse)").matches;
            if (shareButton) {
                shareButton.style.display = isMobileViewport ? "inline-flex" : "none";
            }
            if (window.PF) {
                window.PF("flyerDialog").show();
            }
        } catch (error) {
            showError(error && error.message);
        } finally {
            button.disabled = false;
            button.classList.remove("ui-state-disabled");
        }
    }

    function download() {
        if (!flyerBlob || !flyerFile) {
            return;
        }
        const link = document.createElement("a");
        link.href = URL.createObjectURL(flyerBlob);
        link.download = flyerFile.name;
        document.body.appendChild(link);
        link.click();
        link.remove();
        window.setTimeout(function () { URL.revokeObjectURL(link.href); }, 1000);
    }

    async function share() {
        if (!flyerFile) {
            return;
        }
        if (typeof navigator.share !== "function") {
            showError("O compartilhamento de imagens exige que a aplicação seja acessada por HTTPS no celular. Enquanto isso, use a opção Baixar PNG.");
            return;
        }
        try {
            if (typeof navigator.canShare === "function" && !navigator.canShare({ files: [flyerFile] })) {
                showError("Este navegador não permite compartilhar a imagem. Use a opção Baixar PNG.");
                return;
            }
            await navigator.share({
                files: [flyerFile],
                title: "Flyer do jogo - Arena Match",
                text: "Confira o próximo confronto no Arena Match!"
            });
        } catch (error) {
            if (!error || error.name !== "AbortError") {
                showError("Não foi possível compartilhar o flyer neste dispositivo.");
            }
        }
    }

    window.ArenaMatchFlyer = {
        gerar: generate,
        baixar: download,
        compartilhar: share
    };
})(window, document);
