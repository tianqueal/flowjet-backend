<#macro page>
  <!DOCTYPE html>
  <html
    lang="${locale.toLanguageTag()}"
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:v="urn:schemas-microsoft-com:vml"
    xmlns:o="urn:schemas-microsoft-com:office:office"
  >
    <head>
      <meta charset="UTF-8" />
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

      <meta name="color-scheme" content="light dark" />
      <meta name="supported-color-schemes" content="light dark" />

      <title>${subject}</title>

      <#include "../_partials/_styles.ftl">
    </head>
    <body class="body">
      <table
        width="100%"
        border="0"
        cellspacing="0"
        cellpadding="0"
        class="main-container"
      >
        <tr>
          <td align="center" valign="top" style="padding: 40px 20px">
            <table
              border="0"
              cellpadding="0"
              cellspacing="0"
              width="100%"
              class="main-table"
              style="max-width: 600px"
            >
              <tr>
                <td align="center" class="content-cell">
                  <#include "../_partials/_header.ftl">
                  <br /><br />

                  <#nested>

                  <br /><br />
                  <#include "../_partials/_footer.ftl">
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </body>
  </html>
</#macro>
