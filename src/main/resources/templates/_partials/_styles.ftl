<style type="text/css">
  .body {
    font-family: "Inter", system-ui, -apple-system, BlinkMacSystemFont,
      "Segoe UI", Roboto, Oxygen, Ubuntu, Cantarell, "Open Sans",
      "Helvetica Neue", sans-serif;
    width: 100%;
    -webkit-text-size-adjust: 100%;
    -ms-text-size-adjust: 100%;
    margin: 0;
    padding: 0;
  }

  .main-container {
    background-color: #f4f4f4;
  }

  .main-table {
    background-color: #ffffff;
    border-radius: 8px;
  }

  .content-cell {
    padding: 40px;
  }

  .text-primary {
    /** font-size: 24px; **/
    color: #333333;
  }

  .text-secondary {
    /** font-size: 16px; **/
    color: #555555;
  }

  .link {
    font-size: 12px;
    color: #007bff;
    word-break: break-all;
  }

  .button-primary-cell {
    border-radius: 9px;
    background-color: #007bff;
  }

  .button-primary-link {
    /** font-size: 16px; **/
    font-family: "Inter", system-ui, -apple-system, BlinkMacSystemFont,
      "Segoe UI", Roboto, Oxygen, Ubuntu, Cantarell, "Open Sans",
      "Helvetica Neue", sans-serif;
    color: #ffffff;
    text-decoration: none;
    border-radius: 9px;
    padding: 12px 25px;
    border: 1px solid #007bff;
    display: inline-block;
  }

  @media (prefers-color-scheme: dark) {
    .main-container {
      background-color: #121212 !important;
    }
    .main-table {
      background-color: #1e1e1e !important;
    }
    .text-primary {
      color: #ffffff !important;
    }
    .text-secondary {
      color: #bbbbbb !important;
    }
    .link {
      color: #58a6ff !important;
    }
    .button-primary-cell {
      background-color: #58a6ff !important;
    }
    .button-primary-link {
      border-color: #58a6ff !important;
    }
  }

  [data-ogsc] .main-container {
    background-color: #121212 !important;
  }
  [data-ogsc] .main-table {
    background-color: #1e1e1e !important;
  }
  [data-ogsc] .text-primary {
    color: #ffffff !important;
  }
  [data-ogsc] .text-secondary {
    color: #bbbbbb !important;
  }
  [data-ogsc] .link {
    color: #58a6ff !important;
  }
  [data-ogsc] .button-primary-cell {
    background-color: #58a6ff !important;
  }
  [data-ogsc] .button-primary-link {
    border-color: #58a6ff !important;
  }

  @media screen and (max-width: 620px) {
    .main-table {
      width: 100% !important;
    }

    .content-cell {
      padding: 20px !important;
    }
  }
</style>
