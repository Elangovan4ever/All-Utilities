<Global.Microsoft.VisualBasic.CompilerServices.DesignerGenerated()> _
Partial Class Form1
    Inherits System.Windows.Forms.Form

    'Form overrides dispose to clean up the component list.
    <System.Diagnostics.DebuggerNonUserCode()> _
    Protected Overrides Sub Dispose(ByVal disposing As Boolean)
        Try
            If disposing AndAlso components IsNot Nothing Then
                components.Dispose()
            End If
        Finally
            MyBase.Dispose(disposing)
        End Try
    End Sub

    'Required by the Windows Form Designer
    Private components As System.ComponentModel.IContainer

    'NOTE: The following procedure is required by the Windows Form Designer
    'It can be modified using the Windows Form Designer.  
    'Do not modify it using the code editor.
    <System.Diagnostics.DebuggerStepThrough()> _
    Private Sub InitializeComponent()
        Me.CustomTabControl1 = New System.Windows.Forms.CustomTabControl()
        Me.TabPage1 = New System.Windows.Forms.TabPage()
        Me.TabPage2 = New System.Windows.Forms.TabPage()
        Me.CustomTabControl1.SuspendLayout()
        Me.SuspendLayout()
        '
        'CustomTabControl1
        '
        Me.CustomTabControl1.Controls.Add(Me.TabPage1)
        Me.CustomTabControl1.Controls.Add(Me.TabPage2)
        '
        '
        '
        Me.CustomTabControl1.DisplayStyleProvider.BorderColor = System.Drawing.SystemColors.ControlDark
        Me.CustomTabControl1.DisplayStyleProvider.BorderColorHot = System.Drawing.SystemColors.ControlDark
        Me.CustomTabControl1.DisplayStyleProvider.BorderColorSelected = System.Drawing.Color.FromArgb(CType(CType(127, Byte), Integer), CType(CType(157, Byte), Integer), CType(CType(185, Byte), Integer))
        Me.CustomTabControl1.DisplayStyleProvider.CloserColor = System.Drawing.Color.DarkGray
        Me.CustomTabControl1.DisplayStyleProvider.FocusTrack = True
        Me.CustomTabControl1.DisplayStyleProvider.HotTrack = True
        Me.CustomTabControl1.DisplayStyleProvider.ImageAlign = System.Drawing.ContentAlignment.MiddleLeft
        Me.CustomTabControl1.DisplayStyleProvider.Opacity = 1.0!
        Me.CustomTabControl1.DisplayStyleProvider.Overlap = 0
        Me.CustomTabControl1.DisplayStyleProvider.Padding = New System.Drawing.Point(6, 3)
        Me.CustomTabControl1.DisplayStyleProvider.Radius = 2
        Me.CustomTabControl1.DisplayStyleProvider.ShowTabCloser = False
        Me.CustomTabControl1.DisplayStyleProvider.TextColor = System.Drawing.SystemColors.ControlText
        Me.CustomTabControl1.DisplayStyleProvider.TextColorDisabled = System.Drawing.SystemColors.ControlDark
        Me.CustomTabControl1.DisplayStyleProvider.TextColorSelected = System.Drawing.SystemColors.ControlText
        Me.CustomTabControl1.Dock = System.Windows.Forms.DockStyle.Fill
        Me.CustomTabControl1.Font = New System.Drawing.Font("Microsoft Sans Serif", 8.25!)
        Me.CustomTabControl1.HotTrack = True
        Me.CustomTabControl1.Location = New System.Drawing.Point(0, 0)
        Me.CustomTabControl1.Name = "CustomTabControl1"
        Me.CustomTabControl1.SelectedIndex = 0
        Me.CustomTabControl1.Size = New System.Drawing.Size(948, 482)
        Me.CustomTabControl1.TabIndex = 0
        '
        'TabPage1
        '
        Me.TabPage1.Location = New System.Drawing.Point(4, 23)
        Me.TabPage1.Name = "TabPage1"
        Me.TabPage1.Padding = New System.Windows.Forms.Padding(3)
        Me.TabPage1.Size = New System.Drawing.Size(940, 455)
        Me.TabPage1.TabIndex = 0
        Me.TabPage1.Text = "TabPage1"
        Me.TabPage1.UseVisualStyleBackColor = True
        '
        'TabPage2
        '
        Me.TabPage2.Location = New System.Drawing.Point(4, 23)
        Me.TabPage2.Name = "TabPage2"
        Me.TabPage2.Padding = New System.Windows.Forms.Padding(3)
        Me.TabPage2.Size = New System.Drawing.Size(192, 73)
        Me.TabPage2.TabIndex = 1
        Me.TabPage2.Text = "TabPage2"
        Me.TabPage2.UseVisualStyleBackColor = True
        '
        'Form1
        '
        Me.AutoScaleDimensions = New System.Drawing.SizeF(6.0!, 13.0!)
        Me.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font
        Me.ClientSize = New System.Drawing.Size(948, 482)
        Me.Controls.Add(Me.CustomTabControl1)
        Me.Name = "Form1"
        Me.Text = "Form1"
        Me.CustomTabControl1.ResumeLayout(False)
        Me.ResumeLayout(False)

    End Sub

    Friend WithEvents CustomTabControl1 As CustomTabControl
    Friend WithEvents TabPage1 As TabPage
    Friend WithEvents TabPage2 As TabPage
End Class
