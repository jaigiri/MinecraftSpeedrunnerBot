(function (window, document) {
    "use strict";

    const tabs = {};

    function changeElementClass(element, classValue) {
        if (element.getAttribute("className")) {
            element.setAttribute("className", classValue);
        } else {
            element.setAttribute("class", classValue);
        }
    }

    function getClassAttribute(element) {
        if (element.getAttribute("className")) {
            return element.getAttribute("className");
        } else {
            return element.getAttribute("class");
        }
    }

    function addClass(element, classValue) {
        changeElementClass(element, getClassAttribute(element) + " " + classValue);
    }

    function removeClass(element, classValue) {
        changeElementClass(element, getClassAttribute(element).replace(classValue, ""));
    }

    function initTabs() {
        const container = document.getElementById("tabs");

        tabs.tabs = findTabs(container);
        tabs.titles = findTitles(tabs.tabs);
        tabs.headers = findHeaders(container);
        tabs.select = select;
        tabs.deselectAll = deselectAll;
        tabs.select(0);

        return true;
    }

    function getCheckBox() {
        return document.getElementById("line-wrapping-toggle");
    }

    function getLabelForCheckBox() {
        return document.getElementById("label-for-line-wrapping-toggle");
    }

    function findCodeBlocks() {
        const spans = document.getElementById("tabs").getElementsByTagName("span");
        const codeBlocks = [];
        for (let i = 0; i < spans.length; ++i) {
            if (spans[i].className.indexOf("code") >= 0) {
                codeBlocks.push(spans[i]);
            }
        }
        return codeBlocks;
    }

    function forAllCodeBlocks(operation) {
        const codeBlocks = findCodeBlocks();

        for (let i = 0; i < codeBlocks.length; ++i) {
            operation(codeBlocks[i], "wrapped");
        }
    }

    function toggleLineWrapping() {
        const checkBox = getCheckBox();

        if (checkBox.checked) {
            forAllCodeBlocks(addClass);
        } else {
            forAllCodeBlocks(removeClass);
        }
    }

    function initControls() {
        if (findCodeBlocks().length > 0) {
            const checkBox = getCheckBox();
            const label = getLabelForCheckBox();

            checkBox.onclick = toggleLineWrapping;
            checkBox.checked = false;

            removeClass(label, "hidden");
        }
    }

    function switchTab() {
        const id = this.id.substr(1);

        for (let i = 0; i < tabs.tabs.length; i++) {
            if (tabs.tabs[i].id === id) {
                tabs.select(i);
                break;
            }
        }

        return false;
    }

    function select(i) {
        this.deselectAll();

        changeElementClass(this.tabs[i], "tab selected");
        changeElementClass(this.headers[i], "selected");

        while (this.headers[i].firstChild) {
            this.headers[i].removeChild(this.headers[i].firstChild);
        }

        const h2 = document.createElement("H2");

        h2.appendChild(document.createTextNode(this.titles[i]));
        this.headers[i].appendChild(h2);
    }

    function deselectAll() {
        for (let i = 0; i < this.tabs.length; i++) {
            changeElementClass(this.tabs[i], "tab deselected");
            changeElementClass(this.headers[i], "deselected");

            while (this.headers[i].firstChild) {
                this.headers[i].removeChild(this.headers[i].firstChild);
            }

            const a = document.createElement("A");

            a.setAttribute("id", "ltab" + i);
            a.setAttribute("href", "#tab" + i);
            a.onclick = switchTab;
            a.appendChild(document.createTextNode(this.titles[i]));

            this.headers[i].appendChild(a);
        }
    }

    function findTabs(container) {
        return findChildElements(container, "DIV", "tab");
    }

    function findHeaders(container) {
        const owner = findChildElements(container, "UL", "tabLinks");
        return findChildElements(owner[0], "LI", null);
    }

    function findTitles(tabs) {
        const titles = [];

        for (let i = 0; i < tabs.length; i++) {
            const tab = tabs[i];
            const header = findChildElements(tab, "H2", null)[0];

            header.parentNode.removeChild(header);

            if (header.innerText) {
                titles.push(header.innerText);
            } else {
                titles.push(header.textContent);
            }
        }

        return titles;
    }

    function findChildElements(container, name, targetClass) {
        const elements = [];
        const children = container.childNodes;

        for (let i = 0; i < children.length; i++) {
            const child = children.item(i);

            if (child.nodeType === 1 && child.nodeName === name) {
                if (targetClass && child.className.indexOf(targetClass) < 0) {
                    continue;
                }

                elements.push(child);
            }
        }

        return elements;
    }

    // Entry point.

    window.onload = function () {
        initTabs();
        initControls();
    };
}(window, window.document));