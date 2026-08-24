(function (window, document) {
    "use strict";

    const WIDTH = 1080;
    const HEIGHT = 1350;
    const NEON = "#0ea5e9";
    const CYAN = "#22d3ee";
    const WHITE = "#f8fafc";
    const NAVY = "#020b18";
    const MUTED = "#a9c0d4";
    const DISPLAY_FONT = "Impact, Haettenschweiler, 'Arial Narrow Bold', Arial, sans-serif";
    const BODY_FONT = "Arial, sans-serif";
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

    function drawBackground(ctx, image) {
        drawCover(ctx, image);

        const shade = ctx.createLinearGradient(0, 0, 0, HEIGHT);
        shade.addColorStop(0, "rgba(2, 11, 24, 0.88)");
        shade.addColorStop(0.45, "rgba(3, 16, 31, 0.72)");
        shade.addColorStop(0.72, "rgba(2, 12, 25, 0.87)");
        shade.addColorStop(1, "rgba(1, 8, 18, 0.97)");
        ctx.fillStyle = shade;
        ctx.fillRect(0, 0, WIDTH, HEIGHT);

        drawGlow(ctx, 120, 390, 480, "rgba(14, 165, 233, 0.20)");
        drawGlow(ctx, 960, 390, 480, "rgba(34, 211, 238, 0.17)");
        drawGlow(ctx, WIDTH / 2, 760, 620, "rgba(14, 165, 233, 0.08)");

        const vignette = ctx.createRadialGradient(WIDTH / 2, 560, 260, WIDTH / 2, 560, 820);
        vignette.addColorStop(0, "rgba(0, 0, 0, 0)");
        vignette.addColorStop(0.72, "rgba(0, 0, 0, 0.18)");
        vignette.addColorStop(1, "rgba(0, 0, 0, 0.68)");
        ctx.fillStyle = vignette;
        ctx.fillRect(0, 0, WIDTH, HEIGHT);

        drawStadiumLights(ctx);
        drawEdgeTexture(ctx);
    }

    function drawGlow(ctx, x, y, radius, color) {
        const glow = ctx.createRadialGradient(x, y, 0, x, y, radius);
        glow.addColorStop(0, color);
        glow.addColorStop(1, "rgba(14, 165, 233, 0)");
        ctx.fillStyle = glow;
        ctx.fillRect(x - radius, y - radius, radius * 2, radius * 2);
    }

    function drawStadiumLights(ctx) {
        ctx.save();
        const horizon = 650;
        const beam = ctx.createLinearGradient(0, 560, 0, 760);
        beam.addColorStop(0, "rgba(34, 211, 238, 0.12)");
        beam.addColorStop(1, "rgba(34, 211, 238, 0)");
        ctx.fillStyle = beam;
        ctx.beginPath();
        ctx.moveTo(80, horizon);
        ctx.lineTo(330, 560);
        ctx.lineTo(430, 760);
        ctx.closePath();
        ctx.fill();
        ctx.beginPath();
        ctx.moveTo(1000, horizon);
        ctx.lineTo(750, 560);
        ctx.lineTo(650, 760);
        ctx.closePath();
        ctx.fill();

        for (let index = 0; index < 23; index += 1) {
            const x = 28 + index * 47;
            const radius = index % 4 === 0 ? 4 : 2.2;
            ctx.beginPath();
            ctx.arc(x, horizon + Math.sin(index * 1.7) * 8, radius, 0, Math.PI * 2);
            ctx.fillStyle = index % 3 === 0 ? "rgba(255,255,255,0.72)" : "rgba(34,211,238,0.55)";
            ctx.shadowColor = CYAN;
            ctx.shadowBlur = 14;
            ctx.fill();
        }
        ctx.restore();
    }

    function pseudoRandom(seed) {
        const value = Math.sin(seed * 12.9898) * 43758.5453;
        return value - Math.floor(value);
    }

    function drawEdgeTexture(ctx) {
        ctx.save();
        for (let index = 1; index <= 110; index += 1) {
            const side = index % 2 === 0 ? 1 : -1;
            const x = side < 0 ? pseudoRandom(index) * 95 : WIDTH - pseudoRandom(index) * 95;
            const y = pseudoRandom(index + 200) * HEIGHT;
            const size = 2 + pseudoRandom(index + 400) * 9;
            ctx.fillStyle = index % 5 === 0 ? "rgba(255,255,255,0.12)" : "rgba(34,211,238,0.16)";
            ctx.translate(x, y);
            ctx.rotate(-0.45);
            ctx.fillRect(-size / 2, -1.5, size, 3);
            ctx.setTransform(1, 0, 0, 1, 0, 0);
        }

        ctx.fillStyle = "rgba(14, 165, 233, 0.52)";
        ctx.translate(-42, 290);
        ctx.rotate(-0.42);
        ctx.fillRect(0, 0, 180, 9);
        ctx.setTransform(1, 0, 0, 1, 0, 0);
        ctx.translate(930, 1040);
        ctx.rotate(-0.42);
        ctx.fillRect(0, 0, 190, 9);
        ctx.restore();
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
        const cut = 22;
        ctx.beginPath();
        ctx.moveTo(x + cut, y);
        ctx.lineTo(x + width, y);
        ctx.lineTo(x + width, y + height - cut);
        ctx.lineTo(x + width - cut, y + height);
        ctx.lineTo(x, y + height);
        ctx.lineTo(x, y + cut);
        ctx.closePath();
        ctx.fillStyle = "rgba(2, 12, 27, 0.90)";
        ctx.fill();
        ctx.strokeStyle = "rgba(34, 211, 238, 0.46)";
        ctx.lineWidth = 2;
        ctx.stroke();

        const accent = ctx.createLinearGradient(x, 0, x + width, 0);
        accent.addColorStop(0, CYAN);
        accent.addColorStop(0.5, NEON);
        accent.addColorStop(1, "rgba(14, 165, 233, 0)");
        ctx.fillStyle = accent;
        ctx.fillRect(x + cut, y, width * 0.62, 5);
        ctx.restore();
    }

    function drawShieldImage(ctx, image, centerX, centerY, maxSize, label) {
        const radius = maxSize * 0.57;
        ctx.save();
        ctx.shadowColor = "rgba(14, 165, 233, 0.50)";
        ctx.shadowBlur = 34;
        ctx.beginPath();
        ctx.arc(centerX, centerY, radius, 0, Math.PI * 2);
        const plate = ctx.createRadialGradient(centerX - 35, centerY - 55, 18, centerX, centerY, radius);
        plate.addColorStop(0, "rgba(23, 61, 91, 0.96)");
        plate.addColorStop(0.62, "rgba(5, 24, 43, 0.98)");
        plate.addColorStop(1, "rgba(1, 10, 22, 0.98)");
        ctx.fillStyle = plate;
        ctx.fill();
        ctx.shadowBlur = 0;
        ctx.strokeStyle = "rgba(34, 211, 238, 0.86)";
        ctx.lineWidth = 4;
        ctx.stroke();
        ctx.setLineDash([9, 14]);
        ctx.beginPath();
        ctx.arc(centerX, centerY, radius + 13, 0, Math.PI * 2);
        ctx.strokeStyle = "rgba(248, 250, 252, 0.23)";
        ctx.lineWidth = 2;
        ctx.stroke();
        ctx.setLineDash([]);

        if (!image) {
            drawFallbackShield(ctx, centerX, centerY, maxSize * 0.86);
            ctx.restore();
            drawTeamRole(ctx, centerX, centerY + radius - 5, label);
            return;
        }

        const scale = Math.min((maxSize * 0.88) / image.width, (maxSize * 0.88) / image.height);
        const width = image.width * scale;
        const height = image.height * scale;
        ctx.beginPath();
        ctx.arc(centerX, centerY, radius - 8, 0, Math.PI * 2);
        ctx.clip();
        ctx.shadowColor = "rgba(0, 0, 0, 0.65)";
        ctx.shadowBlur = 20;
        ctx.shadowOffsetY = 8;
        ctx.drawImage(image, centerX - width / 2, centerY - height / 2, width, height);
        ctx.restore();

        drawTeamRole(ctx, centerX, centerY + radius - 5, label);
    }

    function drawTeamRole(ctx, centerX, y, label) {
        ctx.save();
        roundedRect(ctx, centerX - 69, y - 17, 138, 34, 17);
        ctx.fillStyle = "rgba(14, 165, 233, 0.92)";
        ctx.fill();
        ctx.font = "800 16px " + BODY_FONT;
        ctx.fillStyle = WHITE;
        ctx.textAlign = "center";
        ctx.textBaseline = "middle";
        ctx.fillText(label, centerX, y + 1);
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
        const fitted = fitLines(ctx, value, maxWidth, maxLines, startSize, minSize, BODY_FONT);
        ctx.save();
        ctx.font = "900 " + fitted.fontSize + "px " + BODY_FONT;
        ctx.fillStyle = WHITE;
        ctx.textAlign = "center";
        ctx.textBaseline = "middle";
        fitted.lines.forEach(function (line, index) {
            ctx.fillText(line.toUpperCase(), centerX, startY + index * lineHeight);
        });
        ctx.restore();
    }

    function drawSpacedText(ctx, value, centerX, y, spacing) {
        const characters = String(value || "").split("");
        const widths = characters.map(function (character) { return ctx.measureText(character).width; });
        const totalWidth = widths.reduce(function (sum, width) { return sum + width; }, 0)
            + Math.max(0, characters.length - 1) * spacing;
        let x = centerX - totalWidth / 2;
        characters.forEach(function (character, index) {
            ctx.fillText(character, x, y);
            x += widths[index] + spacing;
        });
    }

    function drawAddress(ctx, value) {
        const fitted = fitLines(ctx, value, 720, 3, 34, 25, BODY_FONT);
        ctx.save();
        ctx.font = "800 " + fitted.fontSize + "px " + BODY_FONT;
        ctx.fillStyle = WHITE;
        ctx.textAlign = "left";
        ctx.textBaseline = "middle";
        fitted.lines.forEach(function (line, index) {
            ctx.fillText(line, 220, 1021 + index * 42);
        });
        ctx.restore();
    }

    function drawCalendarIcon(ctx, centerX, centerY) {
        ctx.save();
        ctx.translate(centerX, centerY);
        ctx.strokeStyle = CYAN;
        ctx.fillStyle = "rgba(14, 165, 233, 0.11)";
        ctx.lineWidth = 5;
        roundedRect(ctx, -30, -27, 60, 58, 8);
        ctx.fill();
        ctx.stroke();
        ctx.beginPath();
        ctx.moveTo(-30, -9);
        ctx.lineTo(30, -9);
        ctx.moveTo(-15, -36);
        ctx.lineTo(-15, -19);
        ctx.moveTo(15, -36);
        ctx.lineTo(15, -19);
        ctx.stroke();
        [-15, 0, 15].forEach(function (x) {
            [-1, 13].forEach(function (y) {
                ctx.beginPath();
                ctx.arc(x, y, 2.8, 0, Math.PI * 2);
                ctx.fillStyle = WHITE;
                ctx.fill();
            });
        });
        ctx.restore();
    }

    function drawClockIcon(ctx, centerX, centerY) {
        ctx.save();
        ctx.translate(centerX, centerY);
        ctx.strokeStyle = CYAN;
        ctx.lineWidth = 5;
        ctx.beginPath();
        ctx.arc(0, 0, 32, 0, Math.PI * 2);
        ctx.stroke();
        ctx.beginPath();
        ctx.moveTo(0, 0);
        ctx.lineTo(0, -18);
        ctx.moveTo(0, 0);
        ctx.lineTo(14, 10);
        ctx.stroke();
        ctx.restore();
    }

    function drawPinIcon(ctx, centerX, centerY) {
        ctx.save();
        ctx.translate(centerX, centerY);
        ctx.strokeStyle = CYAN;
        ctx.fillStyle = "rgba(14, 165, 233, 0.13)";
        ctx.lineWidth = 5;
        ctx.beginPath();
        ctx.moveTo(0, 38);
        ctx.bezierCurveTo(-11, 21, -31, 1, -31, -19);
        ctx.arc(0, -19, 31, Math.PI, 0);
        ctx.bezierCurveTo(31, 1, 11, 21, 0, 38);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.beginPath();
        ctx.arc(0, -18, 9, 0, Math.PI * 2);
        ctx.fillStyle = WHITE;
        ctx.fill();
        ctx.restore();
    }

    function drawVersus(ctx, centerX, centerY) {
        ctx.save();
        ctx.translate(centerX, centerY);
        ctx.rotate(-0.10);
        ctx.fillStyle = "rgba(34, 211, 238, 0.28)";
        ctx.fillRect(-62, -58, 124, 116);
        ctx.fillStyle = NAVY;
        ctx.fillRect(-54, -51, 108, 102);
        ctx.rotate(0.10);
        ctx.shadowColor = CYAN;
        ctx.shadowBlur = 24;
        ctx.fillStyle = WHITE;
        ctx.font = "900 53px " + DISPLAY_FONT;
        ctx.textAlign = "center";
        ctx.textBaseline = "middle";
        ctx.fillText("VS", 0, 2);
        ctx.restore();
    }

    function drawBrandMark(ctx, image) {
        if (!image) {
            return;
        }
        const width = 265;
        const height = width * (image.height / image.width);
        ctx.save();
        ctx.globalAlpha = 0.98;
        ctx.drawImage(image, 58, 42, width, height);
        ctx.restore();
    }

    function drawStatusPill(ctx) {
        ctx.save();
        roundedRect(ctx, 742, 42, 278, 52, 26);
        ctx.fillStyle = "rgba(14, 165, 233, 0.14)";
        ctx.fill();
        ctx.strokeStyle = "rgba(34, 211, 238, 0.66)";
        ctx.lineWidth = 2;
        ctx.stroke();
        ctx.beginPath();
        ctx.arc(773, 68, 6, 0, Math.PI * 2);
        ctx.fillStyle = CYAN;
        ctx.shadowColor = CYAN;
        ctx.shadowBlur = 12;
        ctx.fill();
        ctx.shadowBlur = 0;
        ctx.font = "800 17px " + BODY_FONT;
        ctx.fillStyle = WHITE;
        ctx.textAlign = "left";
        ctx.textBaseline = "middle";
        ctx.fillText("PARTIDA CONFIRMADA", 791, 69);
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
            drawBackground(ctx, images[0]);
            drawBrandMark(ctx, images[3]);
            drawStatusPill(ctx);

            ctx.textAlign = "center";
            ctx.textBaseline = "middle";
            ctx.fillStyle = CYAN;
            ctx.font = "800 20px " + BODY_FONT;
            drawSpacedText(ctx, "PRÓXIMO CONFRONTO", WIDTH / 2, 145, 5);
            ctx.fillStyle = WHITE;
            ctx.font = "900 82px " + DISPLAY_FONT;
            ctx.shadowColor = "rgba(14, 165, 233, 0.38)";
            ctx.shadowBlur = 22;
            ctx.fillText("DIA DE JOGO", WIDTH / 2, 213);
            ctx.shadowBlur = 0;

            const titleLine = ctx.createLinearGradient(325, 0, 755, 0);
            titleLine.addColorStop(0, "rgba(34, 211, 238, 0)");
            titleLine.addColorStop(0.25, CYAN);
            titleLine.addColorStop(0.75, NEON);
            titleLine.addColorStop(1, "rgba(14, 165, 233, 0)");
            ctx.fillStyle = titleLine;
            ctx.fillRect(325, 263, 430, 5);

            drawShieldImage(ctx, images[1], 275, 425, 236, "MANDANTE");
            drawShieldImage(ctx, images[2], 805, 425, 236, "VISITANTE");
            drawVersus(ctx, WIDTH / 2, 425);

            drawCenteredLines(ctx, homeName, 275, 608, 400, 2, 42, 28, 44);
            drawCenteredLines(ctx, awayName, 805, 608, 400, 2, 42, 28, 44);

            panel(ctx, 70, 700, 940, 198);
            drawCalendarIcon(ctx, 138, 780);
            drawClockIcon(ctx, 625, 780);

            ctx.textAlign = "left";
            ctx.fillStyle = CYAN;
            ctx.font = "800 20px " + BODY_FONT;
            ctx.fillText("DATA DA PARTIDA", 190, 746);
            ctx.fillText("HORÁRIO", 680, 746);
            ctx.fillStyle = MUTED;
            ctx.font = "800 25px " + BODY_FONT;
            ctx.fillText(weekday.toUpperCase(), 190, 784);
            ctx.fillStyle = WHITE;
            ctx.font = "900 42px " + BODY_FONT;
            ctx.fillText(date, 190, 835);
            ctx.font = "900 68px " + DISPLAY_FONT;
            ctx.fillText(time, 680, 812);

            const divider = ctx.createLinearGradient(0, 725, 0, 870);
            divider.addColorStop(0, "rgba(34, 211, 238, 0)");
            divider.addColorStop(0.5, "rgba(34, 211, 238, 0.66)");
            divider.addColorStop(1, "rgba(34, 211, 238, 0)");
            ctx.fillStyle = divider;
            ctx.fillRect(536, 722, 2, 152);

            panel(ctx, 70, 930, 940, 184);
            drawPinIcon(ctx, 137, 1020);
            ctx.textAlign = "left";
            ctx.fillStyle = CYAN;
            ctx.font = "800 21px " + BODY_FONT;
            ctx.fillText("LOCAL DO JOGO", 220, 974);
            drawAddress(ctx, address);

            ctx.textAlign = "center";
            ctx.fillStyle = CYAN;
            ctx.font = "800 18px " + BODY_FONT;
            drawSpacedText(ctx, "ENTRE EM CAMPO. FAÇA HISTÓRIA.", WIDTH / 2, 1170, 3);
            ctx.fillStyle = "rgba(248, 250, 252, 0.24)";
            ctx.fillRect(170, 1208, 740, 1);

            if (images[3]) {
                const logoWidth = 280;
                const logoHeight = logoWidth * (images[3].height / images[3].width);
                ctx.drawImage(images[3], (WIDTH - logoWidth) / 2, 1241, logoWidth, logoHeight);
            }

            ctx.strokeStyle = "rgba(34, 211, 238, 0.40)";
            ctx.lineWidth = 2;
            ctx.strokeRect(18, 18, WIDTH - 36, HEIGHT - 36);

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
